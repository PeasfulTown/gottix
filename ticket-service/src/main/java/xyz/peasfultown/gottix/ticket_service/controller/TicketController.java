package xyz.peasfultown.gottix.ticket_service.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.gottix.ticket_service.TicketApi;
import xyz.peasfultown.gottix.ticket_service.controller.annotation.IsAdminOrAgent;
import xyz.peasfultown.gottix.ticket_service.controller.annotation.IsCustomer;
import xyz.peasfultown.gottix.ticket_service.exception.ForbiddenException;
import xyz.peasfultown.gottix.ticket_service.model.*;
import xyz.peasfultown.gottix.ticket_service.service.TicketService;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequiredArgsConstructor
public class TicketController implements TicketApi {
    private final TicketService ticketService;

    // ======================================================
    // TICKET GET
    // ======================================================

    @IsAdminOrAgent
    @Override
    public ResponseEntity<PagedTicketSummaryResponse> getAllTickets(
            String xUserId,
            String xUserRole,
            TicketStatus status,
            TicketPriority priority,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "CREATED_AT") SortField sortBy,
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder) throws Exception {
        Page<TicketSummary> tickets = ticketService.getAllTicketsPage(status, priority, sortBy, sortOrder, pageNumber, pageSize);
        PagedTicketSummaryResponse res = PagedTicketSummaryResponse.builder()
                .content(tickets.getContent())
                .page(ResponsePage.builder()
                        .number(tickets.getNumber())
                        .size(tickets.getSize())
                        .totalElements(tickets.getTotalElements())
                        .totalPages(tickets.getTotalPages())
                        .build())
                .build();
        return ok(res);
    }

    @Override
    public ResponseEntity<Ticket> getTicket(
            String xUserId,
            String xUserRole,
            String ticketId) throws Exception {
        Ticket t = null;
        if (xUserRole.equals("ADMIN")
            || xUserRole.equals("AGENT"))
            t = ticketService.getTicket(ticketId);
        else
            t = ticketService.getTicketByCustomerIdAndTicketId(xUserId, ticketId);

        return ok(t);
    }

    @IsAdminOrAgent
    @Override
    public ResponseEntity<PagedTicketSummaryResponse> getAssignedTickets(
            String xUserId,
            String xUserRole,
            TicketStatus status,
            TicketPriority priority,
            @RequestParam(defaultValue = "CREATED_AT") SortField sortBy,
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) throws Exception {
        Page<TicketSummary> ticketSummaryPage = ticketService.getAssignedTickets(xUserId, status, priority, sortBy, sortOrder, pageNumber, pageSize);
        PagedTicketSummaryResponse res = PagedTicketSummaryResponse.builder()
                .content(ticketSummaryPage.getContent())
                .page(ResponsePage.builder()
                        .number(ticketSummaryPage.getNumber())
                        .size(ticketSummaryPage.getSize())
                        .totalElements(ticketSummaryPage.getTotalElements())
                        .totalPages(ticketSummaryPage.getTotalPages())
                        .build())
                .build();
        return ok(res);
    }

    @IsCustomer
    @Override
    public ResponseEntity<PagedTicketSummaryResponse> getCustomerOwnedTickets(
            String xUserId,
            String xUserRole,
            TicketStatus status,
            TicketPriority priority,
            @RequestParam(defaultValue = "CREATED_AT") SortField sortBy, // TODO: FIX THESE ENUMS
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) throws Exception {
        Page<TicketSummary> ticketSummaryPage = ticketService.getCustomerOwnedTickets(xUserId, status, priority, sortBy, sortOrder, pageNumber, pageSize);
        PagedTicketSummaryResponse res = PagedTicketSummaryResponse.builder()
                .content(ticketSummaryPage.getContent())
                .page(ResponsePage.builder()
                        .number(ticketSummaryPage.getNumber())
                        .size(ticketSummaryPage.getSize())
                        .totalElements(ticketSummaryPage.getTotalElements())
                        .totalPages(ticketSummaryPage.getTotalPages())
                        .build())
                .build();
        return ok(res);
    }

    // ======================================================
    // TICKET POST,PATCH,DELETE
    // ======================================================

    @Override
    public ResponseEntity<Ticket> createTicket(
            String xUserId,
            String xUserRole,
            TicketCreateRequest ticketCreateRequest) throws Exception {
        if (xUserRole.equals("CUSTOMER") && !xUserId.equals(ticketCreateRequest.getCustomerId()))
            throw new ForbiddenException("user not allowed to create ticket for another customer");

        return status(HttpStatus.CREATED).body(ticketService.createTicket(ticketCreateRequest));
    }

    @Override
    public ResponseEntity<TicketSummary> updateTicket(
            String xUserId,
            String xUserRole,
            String ticketId,
            TicketUpdateRequest ticketUpdateRequest) throws Exception {
        return ok(ticketService.updateTicket(xUserId, xUserRole, ticketId, ticketUpdateRequest));
    }

    @Override
    public ResponseEntity<Void> deleteTicket(
            String xUserId,
            String xUserRole,
            String ticketId) throws Exception {
        if (xUserRole.equals("ADMIN"))
            ticketService.deleteTicket(ticketId);
        if (xUserRole.equals("AGENT"))
            throw new ForbiddenException("agent not allowed to delete ticket");

        ticketService.deleteTicket(xUserId, ticketId);

        return status(HttpStatus.NO_CONTENT).build();
    }

    // ======================================================
    // TICKET WORKFLOW
    // ======================================================

    @IsAdminOrAgent
    @Override
    public ResponseEntity<Void> assignTicketToAgent(
            String xUserId,
            String xUserRole,
            String ticketId,
            TicketAssignRequest ticketAssignRequest) throws Exception {
        ticketService.assignTicket(ticketId, ticketAssignRequest.getAgentId());

        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> reopenTicket(
            String xUserId,
            String xUserRole,
            String ticketId,
            TicketReopenRequest ticketReopenRequest) throws Exception {
        if (xUserRole.equals("CUSTOMER")
                && (ticketReopenRequest == null
                || ticketReopenRequest.getReason() == null
                || ticketReopenRequest.getReason().isBlank()))
            throw new ForbiddenException("user not allowed to reopen ticket without a reason");

        if (xUserRole.equals("ADMIN") || xUserRole.equals("AGENT")) {
            ticketService.reopenTicket(ticketId, ticketReopenRequest == null ? null : ticketReopenRequest.getReason());
        } else {
            ticketService.reopenTicket(xUserId, ticketId, ticketReopenRequest.getReason());
        }

        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> updateTicketStatus(
            String xUserId,
            String xUserRole,
            String ticketId,
            TicketStatusUpdateRequest ticketStatusUpdateRequest) throws Exception {
        if (xUserRole.equals("ADMIN"))
            ticketService.updateTicketStatus(ticketId, ticketStatusUpdateRequest.getStatus());
        else
            ticketService.updateTicketStatus(xUserId, xUserRole, ticketId, ticketStatusUpdateRequest.getStatus());
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> updateTicketPriority(
            String xUserId,
            String xUserRole,
            String ticketId,
            TicketPriorityUpdateRequest ticketPriorityUpdateRequest) throws Exception {
        ticketService.updateTicketPriority(xUserId, xUserRole, ticketId, ticketPriorityUpdateRequest.getPriority());
        return status(HttpStatus.NO_CONTENT).build();
    }

    // ======================================================
    // TICKET COMMENT
    // ======================================================


    @Override
    public ResponseEntity<Comment> createTicketComment(
            String xUserId,
            String xUserRole,
            String ticketId,
            CommentCreateRequest commentCreateRequest) throws Exception {
        Comment newComment = ticketService.addTicketComment(ticketId, xUserId, xUserRole, commentCreateRequest.getBody());
        return status(HttpStatus.CREATED).body(newComment);
    }

    @Override
    public ResponseEntity<Comment> editTicketComment(
            String xUserId,
            String xUserRole,
            String ticketId,
            String commentId,
            CommentEditRequest commentEditRequest) throws Exception {

        return ok(ticketService.editTicketComment(xUserId, xUserRole, ticketId, commentId, commentEditRequest.getBody()));
    }

    @Override
    public ResponseEntity<Void> deleteTicketComment(
            String xUserId,
            String xUserRole,
            String ticketId,
            String commentId) throws Exception {
        if (xUserRole.equals("ADMIN"))
            ticketService.deleteTicketComment(ticketId, commentId);
        else {
            ticketService.deleteTicketComment(xUserId, ticketId, commentId);
        }
        return status(HttpStatus.NO_CONTENT).build();
    }

}
