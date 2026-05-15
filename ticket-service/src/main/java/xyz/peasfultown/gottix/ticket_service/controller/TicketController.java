package xyz.peasfultown.gottix.ticket_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.gottix.ticket_service.TicketApi;
import xyz.peasfultown.gottix.ticket_service.model.*;

@RestController
public class TicketController implements TicketApi {
    @Override
    public ResponseEntity<Void> assignTicketToAgent(String xUserId, String xUserRole, String ticketId, TicketAssignRequest ticketAssignRequest) throws Exception {
        return TicketApi.super.assignTicketToAgent(xUserId, xUserRole, ticketId, ticketAssignRequest);
    }

    @Override
    public ResponseEntity<Comment> createTicketComment(String xUserId, String xUserRole, String ticketId, CommentCreateRequest commentCreateRequest) throws Exception {
        return TicketApi.super.createTicketComment(xUserId, xUserRole, ticketId, commentCreateRequest);
    }

    @Override
    public ResponseEntity<Void> deleteTicketComment(String xUserId, String xUserRole, String ticketId, String commentId) throws Exception {
        return TicketApi.super.deleteTicketComment(xUserId, xUserRole, ticketId, commentId);
    }

    @Override
    public ResponseEntity<Ticket> deleteTicketCustomerOwnerAgentAdmin(String xUserId, String xUserRole, String ticketId) throws Exception {
        return TicketApi.super.deleteTicketCustomerOwnerAgentAdmin(xUserId, xUserRole, ticketId);
    }

    @Override
    public ResponseEntity<Comment> editTicketCommentOwner(String xUserId, String xUserRole, String ticketId, String commentId, CommentEditRequest commentEditRequest) throws Exception {
        return TicketApi.super.editTicketCommentOwner(xUserId, xUserRole, ticketId, commentId, commentEditRequest);
    }

    @Override
    public ResponseEntity<PagedTicketSummaryResponse> getAllTickets(String xUserId, String xUserRole, TicketStatus status, TicketPriority priority, Integer pageNumber, Integer pageSize, SortField sortBy, SortOrder sortOrder) throws Exception {
        return TicketApi.super.getAllTickets(xUserId, xUserRole, status, priority, pageNumber, pageSize, sortBy, sortOrder);
    }

    @Override
    public ResponseEntity<PagedTicketSummaryResponse> getAssignedTicketsAgentAdmin(String xUserId, String xUserRole, TicketStatus status, TicketPriority priority, Integer pageNumber, Integer pageSize, SortField sortBy, SortOrder sortOrder) throws Exception {
        return TicketApi.super.getAssignedTicketsAgentAdmin(xUserId, xUserRole, status, priority, pageNumber, pageSize, sortBy, sortOrder);
    }

    @Override
    public ResponseEntity<PagedTicketSummaryResponse> getCustomerOwnedTickets(String xUserId, String xUserRole, TicketStatus status, TicketPriority priority, Integer pageNumber, Integer pageSize, SortField sortBy, SortOrder sortOrder) throws Exception {
        return TicketApi.super.getCustomerOwnedTickets(xUserId, xUserRole, status, priority, pageNumber, pageSize, sortBy, sortOrder);
    }

    @Override
    public ResponseEntity<Ticket> getTicketCustomerOwnerAgentAdmin(String xUserId, String xUserRole, String ticketId) throws Exception {
        return TicketApi.super.getTicketCustomerOwnerAgentAdmin(xUserId, xUserRole, ticketId);
    }

    @Override
    public ResponseEntity<Ticket> getTickets(String xUserId, String xUserRole, TicketCreateRequest ticketCreateRequest) throws Exception {
        return TicketApi.super.getTickets(xUserId, xUserRole, ticketCreateRequest);
    }

    @Override
    public ResponseEntity<Void> reopenTicket(String xUserId, String xUserRole, String ticketId, TicketReopenRequest ticketReopenRequest) throws Exception {
        return TicketApi.super.reopenTicket(xUserId, xUserRole, ticketId, ticketReopenRequest);
    }

    @Override
    public ResponseEntity<Ticket> updateTicket(String xUserId, String xUserRole, String ticketId, TicketUpdateRequest ticketUpdateRequest) throws Exception {
        return TicketApi.super.updateTicket(xUserId, xUserRole, ticketId, ticketUpdateRequest);
    }

    @Override
    public ResponseEntity<Void> updateTicketPriority(String xUserId, String xUserRole, String ticketId, TicketPriorityUpdateRequest ticketPriorityUpdateRequest) throws Exception {
        return TicketApi.super.updateTicketPriority(xUserId, xUserRole, ticketId, ticketPriorityUpdateRequest);
    }

    @Override
    public ResponseEntity<Void> updateTicketStatus(String xUserId, String xUserRole, String ticketId, TicketStatusUpdateRequest ticketStatusUpdateRequest) throws Exception {
        return TicketApi.super.updateTicketStatus(xUserId, xUserRole, ticketId, ticketStatusUpdateRequest);
    }
}
