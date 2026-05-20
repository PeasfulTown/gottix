package xyz.peasfultown.gottix.ticket_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.gottix.ticket_service.config.RabbitMqConfig;
import xyz.peasfultown.gottix.ticket_service.dto.NotificationType;
import xyz.peasfultown.gottix.ticket_service.dto.TicketChangeNotificationEvent;
import xyz.peasfultown.gottix.ticket_service.entity.CommentEntity;
import xyz.peasfultown.gottix.ticket_service.entity.EventType;
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

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objMapper;

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
        String sortField = switch (sortBy) {
            case CREATED_AT ->  "createdAt";
            case UPDATED_AT -> "updatedAt";
        };

        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                sortOrder == SortOrder.DESC
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending()
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
        String sortField = switch (sortBy) {
            case CREATED_AT ->  "createdAt";
            case UPDATED_AT -> "updatedAt";
        };

        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                sortOrder == SortOrder.DESC
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending()
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
        String sortField = switch (sortBy) {
            case CREATED_AT ->  "createdAt";
            case UPDATED_AT -> "updatedAt";
        };

        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                sortOrder == SortOrder.DESC
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending()
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

        te = ticketRepo.save(te);
        outboxService.saveTicketToOutbox(te, EventType.CREATE);
        sendNotification(buildTicketCreateNotification(te.getId().toString(), req.getCustomerId()));

        return ticketMapper.toModel(te);
    }

    @Override
    public Ticket createCustomerTicket(String userId, TicketCreateRequest req) {
        TicketEntity te = TicketEntity.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .priority(TicketEntity.TicketPriority.fromValue(req.getPriority().getValue()))
                .customerId(UUID.fromString(userId))
                .build();

        te = ticketRepo.save(te);
        outboxService.saveTicketToOutbox(te, EventType.CREATE);
        sendNotification(buildTicketCreateNotification(te.getId().toString(), userId));

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
        outboxService.saveTicketToOutbox(te, EventType.UPDATE);
        // TODO: send ticket updated notification
        return ticketMapper.toModelSummary(te);
    }

    @Override
    public void deleteTicket(String ticketId) {
        try {
            outboxService.saveTicketToOutbox(ticketRepo.getReferenceById(UUID.fromString(ticketId)), EventType.DELETE);
            ticketRepo.deleteById(UUID.fromString(ticketId));
            // TODO: send ticket deleted notification?
        } catch (
                EntityNotFoundException e) {
            log.error("unable to delete document id {}", ticketId, e);
            throw new TicketNotFoundException(ticketId);
        }
    }

    @Override
    public void deleteTicket(String userId, String ticketId) {
        TicketEntityIdsOnlyProjection teids = ticketRepo.findIdsOnlyById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (!teids.getCustomerId().toString().equals(userId))
            throw new ForbiddenException("user not allowed to delete ticket they don't own");

        outboxService.saveTicketToOutbox(ticketRepo.getReferenceById(UUID.fromString(ticketId)), EventType.DELETE);
        ticketRepo.deleteById(UUID.fromString(ticketId));
        // TODO: send ticket deleted notification?
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
        outboxService.saveTicketToOutbox(te, EventType.UPDATE);
        sendNotification(buildTicketAssignedNotification(te.getId().toString(), agentId));
    }

    @Override
    public void reopenTicket(String ticketId, String reason) {
        // TODO: save reason for reopening ticket
        TicketEntity te = ticketRepo.findById(UUID.fromString(ticketId))
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (te.getStatus() == TicketEntity.TicketStatus.OPENED)
            throw new TicketStatusUpdateException("cannot reopen an already open ticket");
        te.setStatus(TicketEntity.TicketStatus.OPENED);
        te = ticketRepo.save(te);
        outboxService.saveTicketToOutbox(te, EventType.UPDATE);
        sendNotification(buildTicketReopenedNotification(te.getId().toString(), te.getCustomerId().toString()));
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
        te = ticketRepo.save(te);
        outboxService.saveTicketToOutbox(te, EventType.UPDATE);

        // since customer is the one reopening the ticket, send the
        // notification to the assigned agent
        if (te.getAssignedAgentId() != null)
            sendNotification(buildTicketReopenedNotification(te.getId().toString(), te.getAssignedAgentId().toString()));
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
        te = ticketRepo.save(te);
        outboxService.saveTicketToOutbox(te, EventType.UPDATE);
        sendNotification(buildTicketStatusChangedNotification(te.getId().toString(), te.getCustomerId().toString()));
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
        te = ticketRepo.save(te);
        outboxService.saveTicketToOutbox(te, EventType.UPDATE);
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
            outboxService.saveCommentToOutbox(ce, EventType.CREATE);
        } catch (DataIntegrityViolationException e) {
            throw new TicketNotFoundException(ticketId);
        }
        if (userRole.equals("AGENT"))
            sendNotification(buildTicketCommentAddedNotification(ticketId, teids.getCustomerId().toString()));
        if (userRole.equals("CUSTOMER"))
            if (teids.getAssignedAgentId() != null)
                sendNotification(buildTicketCommentAddedNotification(ticketId, teids.getAssignedAgentId().toString()));
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

        if (!userId.equals(ce.getAuthorId().toString()))
            throw new ForbiddenException("user not allowed to edit comment they don't own");

        if (newCommentBody == null || newCommentBody.isBlank())
            throw new EmptyCommentException("unable to update comment with a blank body");

        ce.setBody(newCommentBody);
        ce = commentRepo.save(ce);
        outboxService.saveCommentToOutbox(ce, EventType.UPDATE);
        return commentMapper.toModel(ce);
    }

    @Override
    public void deleteTicketComment(String ticketId, String commentId) {
        CommentEntity ce = commentRepo.findByTicketIdAndCommentId(UUID.fromString(ticketId), UUID.fromString(commentId))
                .orElseThrow(() -> new CommentNotFoundException(String.format(
                        "comment ID: %s not found for ticket ID: %s", commentId, ticketId
                )));

        outboxService.saveCommentToOutbox(ce, EventType.DELETE);
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

        outboxService.saveCommentToOutbox(ce, EventType.DELETE);
        commentRepo.delete(ce);
        ce.getTicket().getComments().remove(ce);
    }

    private void sendNotification(TicketChangeNotificationEvent event) {
        try {
            Message m = MessageBuilder.withBody(objMapper.writeValueAsBytes(event))
                    .setHeader("__TypeId__", "NotificationEvent")
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
            rabbitTemplate.send(RabbitMqConfig.exchange, RabbitMqConfig.ticket_change_notify_routingKey, m);
        } catch (JsonProcessingException e) {
            log.error("unable to send ticket {} notification", event.getType(), e);
        }
    }

    private TicketChangeNotificationEvent buildTicketCreateNotification(
            String ticketId,
            String receiverId) {
        return TicketChangeNotificationEvent.builder()
                .ticketId(ticketId)
                .receiverId(receiverId)
                .message(String.format(
                        "Your support ticket (ID: %s) has been created.",
                        ticketId
                ))
                .type(NotificationType.TICKET_CREATED)
                .build();
    }

    private TicketChangeNotificationEvent buildTicketAssignedNotification(
            String ticketId,
            String receiverId) {
        return TicketChangeNotificationEvent.builder()
                .ticketId(ticketId)
                .receiverId(receiverId)
                .message(String.format(
                        "Ticket (ID: %s) has been assigned to you.",
                        ticketId
                ))
                .type(NotificationType.TICKET_ASSIGNED)
                .build();
    }

    private TicketChangeNotificationEvent buildTicketStatusChangedNotification(
            String ticketId,
            String receiverId) {
        return TicketChangeNotificationEvent.builder()
                .ticketId(ticketId)
                .receiverId(receiverId)
                .message(String.format(
                        "Ticket (ID: %s) status has been updated.",
                        ticketId
                ))
                .type(NotificationType.TICKET_STATUS_CHANGED)
                .build();
    }

    private TicketChangeNotificationEvent buildTicketResolvedNotification(
            String ticketId,
            String receiverId) {
        return TicketChangeNotificationEvent.builder()
                .ticketId(ticketId)
                .receiverId(receiverId)
                .message(String.format(
                        "Ticket (ID: %s) has been marked as resolved.",
                        ticketId
                ))
                .type(NotificationType.TICKET_STATUS_CHANGED)
                .build();
    }

    private TicketChangeNotificationEvent buildTicketClosedNotification(
            String ticketId,
            String receiverId) {
        return TicketChangeNotificationEvent.builder()
                .ticketId(ticketId)
                .receiverId(receiverId)
                .message(String.format(
                        "Ticket (ID: %s) has been closed.",
                        ticketId
                ))
                .type(NotificationType.TICKET_CLOSED)
                .build();
    }

    private TicketChangeNotificationEvent buildTicketReopenedNotification(
            String ticketId,
            String receiverId) {
        return TicketChangeNotificationEvent.builder()
                .ticketId(ticketId)
                .receiverId(receiverId)
                .message(String.format(
                        "Ticket (ID: %s) has been reopened.",
                        ticketId
                ))
                .type(NotificationType.TICKET_REOPENED)
                .build();
    }


    private TicketChangeNotificationEvent buildTicketCommentAddedNotification(
            String ticketId,
            String receiverId) {
        return TicketChangeNotificationEvent.builder()
                .ticketId(ticketId)
                .receiverId(receiverId)
                .message(String.format(
                        "Ticket (ID: %s) has a new comment.",
                        ticketId
                ))
                .type(NotificationType.COMMENT_ADDED)
                .build();
    }
}
