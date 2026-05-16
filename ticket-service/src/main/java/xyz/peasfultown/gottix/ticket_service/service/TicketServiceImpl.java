package xyz.peasfultown.gottix.ticket_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.gottix.ticket_service.entity.CommentEntity;
import xyz.peasfultown.gottix.ticket_service.entity.TicketEntity;
import xyz.peasfultown.gottix.ticket_service.entity.mapper.CommentMapper;
import xyz.peasfultown.gottix.ticket_service.entity.mapper.TicketMapper;
import xyz.peasfultown.gottix.ticket_service.entity.projection.TicketEntityIdsOnlyProjection;
import xyz.peasfultown.gottix.ticket_service.entity.projection.TicketEntitySummaryProjection;
import xyz.peasfultown.gottix.ticket_service.exception.*;
import xyz.peasfultown.gottix.ticket_service.model.*;
import xyz.peasfultown.gottix.ticket_service.repository.CommentRepository;
import xyz.peasfultown.gottix.ticket_service.repository.TicketRepository;

import java.util.UUID;

import static xyz.peasfultown.gottix.ticket_service.repository.specification.TicketSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepo;
    private final CommentRepository commentRepo;

    private final OutboxService outboxService;

    private final TicketMapper ticketMapper;
    private final CommentMapper commentMapper;

    // ======================================================
    // TICKET GET
    // ======================================================

    @Override
    public Page<TicketSummary> getAllTicketsPage(
            TicketStatus status,
            TicketPriority priority,
            SortField sortBy,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                sortOrder == SortOrder.DESC
                        ? Sort.by(sortBy.getValue()).descending()
                        : Sort.by(sortBy.getValue()).ascending()
        );

        return ticketRepo.findBy(
                hasStatus(status == null ? null : TicketEntity.TicketStatus.fromValue(status.getValue()))
                        .and(priority == null ? null : hasPriority(TicketEntity.TicketPriority.fromValue(priority.getValue()))),
                q -> q.as(TicketEntitySummaryProjection.class).page(pageable)
        ).map(ticketMapper::toModelSummary);
    }

    @Override
    public Ticket getTicket(String ticketId) {
        TicketEntity te = ticketRepo.findByIdPopulateComments(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        return ticketMapper.toModel(te);
    }

    @Override
    public Ticket getTicketByCustomerIdAndTicketId(
            String userId,
            String ticketId) {
        TicketEntity te = ticketRepo.findByCustomerIdAndTicketId(UUID.fromString(userId), UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId, userId));

        return ticketMapper.toModel(te);
    }

    @Override
    public Page<TicketSummary> getAssignedTickets(
            String userId,
            TicketStatus status,
            TicketPriority priority,
            SortField sortBy,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                sortOrder == SortOrder.DESC
                        ? Sort.by(sortBy.getValue()).descending()
                        : Sort.by(sortBy.getValue()).ascending()
        );

        return ticketRepo.findBy(
                hasAssignedAgentId(UUID.fromString(userId))
                .and(hasStatus(status == null ? null : TicketEntity.TicketStatus.fromValue(status.getValue())))
                        .and(hasPriority( priority == null ? null : TicketEntity.TicketPriority.fromValue(priority.getValue()))),
                q -> q.as(TicketEntitySummaryProjection.class).page(pageable)
        ).map(ticketMapper::toModelSummary);
    }

    @Override
    public Page<TicketSummary> getCustomerOwnedTickets(
            String userId,
            TicketStatus status,
            TicketPriority priority,
            SortField sortBy,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                sortOrder == SortOrder.DESC
                        ? Sort.by(sortBy.getValue()).descending()
                        : Sort.by(sortBy.getValue()).ascending()
        );

        return ticketRepo.findBy(
                hasCustomerId(UUID.fromString(userId))
                        .and(hasStatus(status == null ? null : TicketEntity.TicketStatus.fromValue(status.getValue())))
                        .and(hasPriority(priority == null ? null : TicketEntity.TicketPriority.fromValue(priority.getValue()))),
                q -> q.as(TicketEntitySummaryProjection.class).page(pageable)
        ).map(ticketMapper::toModelSummary);
    }

    // ======================================================
    // TICKET MODIFICATION
    // ======================================================

    @Override
    public Ticket createTicket(TicketCreateRequest req) {
        TicketEntity te = TicketEntity.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .priority(TicketEntity.TicketPriority.fromValue(req.getPriority().getValue()))
                .customerId(UUID.fromString(req.getCustomerId()))
                .build();

        try {
            te = ticketRepo.save(te);
            outboxService.saveTicketToOutbox(te);
        } catch (Exception e) {
            log.error("unable to save ticket", e);
        }

        return ticketMapper.toModel(te);
    }

    @Override
    public TicketSummary updateTicket(
            String userId,
            String userRole,
            String ticketId,
            TicketUpdateRequest req) {
        TicketEntity te = ticketRepo.findById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (!userRole.equals("ADMIN")
            && !userId.equals(te.getCustomerId().toString()))
            throw new ForbiddenException("user not allowed to update ticket they don't own");

        if (req.getTitle() != null && !req.getTitle().isBlank())
            te.setTitle(req.getTitle());
        if (req.getDescription() != null && !req.getDescription().isBlank())
            te.setDescription(req.getDescription());
        if (req.getPriority() != null)
            te.setPriority(TicketEntity.TicketPriority.fromValue(req.getPriority().getValue()));
        te = ticketRepo.save(te);
        return ticketMapper.toModelSummary(te);
    }

    @Override
    public void deleteTicket(String ticketId) {
        ticketRepo.deleteById(UUID.fromString(ticketId));
    }

    @Override
    public void deleteTicket(String userId, String ticketId) {
        TicketEntityIdsOnlyProjection teids = ticketRepo.findIdsOnlyById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (!teids.getCustomerId().toString().equals(userId))
            throw new ForbiddenException("user not allowed to delete ticket they don't own");

        ticketRepo.deleteById(UUID.fromString(ticketId));
    }

    // ======================================================
    // TICKET WORKFLOW
    // ======================================================

    @Override
    public void assignTicket(String ticketId, String agentId) {
        TicketEntity te = ticketRepo.findById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        te.setAssignedAgentId(agentId == null ? null : UUID.fromString(agentId));
        ticketRepo.save(te);
    }

    @Override
    public void reopenTicket(String ticketId, String reason) {
        // TODO: save reason for reopening ticket
        TicketEntity te = ticketRepo.findById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (te.getStatus() == TicketEntity.TicketStatus.OPENED)
            throw new TicketStatusUpdateException("cannot reopen an already open ticket");
        te.setStatus(TicketEntity.TicketStatus.OPENED);
        ticketRepo.save(te);
    }

    @Override
    public void reopenTicket(String userId, String ticketId, String reason) {
        // TODO: save reason for reopening ticket
        TicketEntity te = ticketRepo.findById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (te.getStatus() == TicketEntity.TicketStatus.OPENED)
            throw new TicketStatusUpdateException("cannot reopen an already open ticket");

        if (!userId.equals(te.getCustomerId().toString()))
            throw new ForbiddenException("user not allowed to reopen ticket they don't own");

        te.setStatus(TicketEntity.TicketStatus.OPENED);
        ticketRepo.save(te);
    }

    @Override
    public void updateTicketStatus(String ticketId, TicketStatus status) {
        TicketEntity te = ticketRepo.findById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        if (te.getStatus() == TicketEntity.TicketStatus.CLOSED)
            throw new TicketStatusUpdateException("cannot update status of closed ticket");

        TicketEntity.TicketStatus newStatus = TicketEntity.TicketStatus.fromValue(status.getValue());

        if (getTicketStatusHierarchy(newStatus) < getTicketStatusHierarchy(te.getStatus()))
            throw new TicketStatusUpdateException(String.format(
                    "cannot perform backward ticket status update: %s -> %s", te.getStatus(), newStatus
            ));

        te.setStatus(newStatus);
        ticketRepo.save(te);
    }

    @Override
    public void updateTicketStatus(String userId, String userRole, String ticketId, TicketStatus status) {
        TicketEntity te = ticketRepo.findById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (!userId.equals(te.getCustomerId().toString()) && !userRole.equals("AGENT"))
            throw new ForbiddenException("user cannot update status of a ticket they don't own");

        if (te.getStatus() == TicketEntity.TicketStatus.CLOSED)
            throw new TicketStatusUpdateException("cannot update status of a closed ticket");

        if (te.getStatus() == TicketEntity.TicketStatus.RESOLVED)
            throw new TicketStatusUpdateException("cannot update status of a resolved ticket");

        TicketEntity.TicketStatus newStatus = TicketEntity.TicketStatus.fromValue(status.getValue());

        if (newStatus == TicketEntity.TicketStatus.IN_PROGRESS && userRole.equals("CUSTOMER"))
            throw new ForbiddenException("user cannot set ticket status as in progress");

        if (getTicketStatusHierarchy(newStatus) < getTicketStatusHierarchy(te.getStatus()))
            throw new TicketStatusUpdateException(String.format(
                    "cannot perform backward ticket status update: %s -> %s", te.getStatus(), newStatus
            ));

        te.setStatus(newStatus);
        ticketRepo.save(te);
    }

    private int getTicketStatusHierarchy(TicketEntity.TicketStatus status) {
        switch (status) {
            case OPENED -> { return 0; }
            case IN_PROGRESS -> { return 1 ;}
            case RESOLVED -> { return 2 ;}
            case CLOSED -> { return 3 ;}
        }
        throw new IllegalArgumentException("unable to determine ticket status");
    }

    @Override
    public void updateTicketPriority(String userId, String userRole, String ticketId, TicketPriority priority) {
        TicketEntity te = ticketRepo.findById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (userRole.equals("CUSTOMER") && !userId.equals(te.getCustomerId().toString()))
            throw new ForbiddenException("user not allowed to modify ticket they don't own");

        te.setPriority(TicketEntity.TicketPriority.fromValue(priority.getValue()));
        ticketRepo.save(te);
    }

    // ======================================================
    // TICKET COMMENT
    // ======================================================

    @Override
    public Comment addTicketComment(String ticketId, String userId, String userRole, String body) {
        TicketEntityIdsOnlyProjection teids = ticketRepo.findIdsOnlyById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (!teids.getCustomerId().toString().equals(userId) && userRole.equals("CUSTOMER"))
            throw new ForbiddenException("user not allowed to comment on a ticket they do not own");

        if (body == null || body.isBlank())
            throw new EmptyCommentException("unable to create comment with empty body");

        TicketEntity te = ticketRepo.getReferenceById(UUID.fromString(ticketId));

        CommentEntity ce = CommentEntity.builder()
                .authorId(UUID.fromString(userId))
                .ticket(te)
                .authorId(UUID.fromString(userId))
                .body(body)
                .build();

        try {
            ce = commentRepo.save(ce);
            ce.getTicket().getComments().add(ce);
        } catch (DataIntegrityViolationException e) {
            throw new TicketNotFoundException(ticketId);
        }

        return commentMapper.toModel(ce);
    }

    @Override
    public Comment editTicketComment(
            String userId,
            String userRole,
            String ticketId,
            String commentId,
            String newCommentBody) {
        CommentEntity ce = commentRepo.findByTicketIdAndCommentId(UUID.fromString(ticketId), UUID.fromString(commentId))
                .orElseThrow(() -> new CommentNotFoundException(String.format(
                        "comment ID: %s not found for ticket ID: %s", commentId, ticketId
                )));

        if (!userId.equals(ce.getAuthorId().toString()) && !userRole.equals("ADMIN"))
            throw new ForbiddenException("user not allowed to edit comment they don't own");

        if (newCommentBody == null || newCommentBody.isBlank())
            throw new EmptyCommentException("unable to update comment with a blank body");

        ce.setBody(newCommentBody);
        ce = commentRepo.save(ce);

        return commentMapper.toModel(ce);
    }

    @Override
    public void deleteTicketComment(String ticketId, String commentId) {
        CommentEntity ce = commentRepo.findByTicketIdAndCommentId(UUID.fromString(ticketId), UUID.fromString(commentId))
                .orElseThrow(() -> new CommentNotFoundException(String.format(
                        "comment ID: %s not found for ticket ID: %s", commentId, ticketId
                )));

        commentRepo.delete(ce);
        ce.getTicket().getComments().remove(ce);
    }

    @Override
    public void deleteTicketComment(String userId, String ticketId, String commentId) {
        CommentEntity ce = commentRepo.findByTicketIdAndCommentId(UUID.fromString(ticketId), UUID.fromString(commentId))
                .orElseThrow(() -> new CommentNotFoundException(String.format(
                        "comment ID: %s not found for ticket ID: %s", commentId, ticketId
                )));

        if (!ce.getAuthorId().toString().equals(userId))
            throw new ForbiddenException("user not allowed to delete comment they don't own");

        commentRepo.delete(ce);
        ce.getTicket().getComments().remove(ce);
    }


}
