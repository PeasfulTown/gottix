package xyz.peasfultown.gottix.ticket_service.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import xyz.peasfultown.gottix.ticket_service.model.*;

public interface TicketService {

    // ======================================================
    // TICKET GET
    // ======================================================

    Page<TicketSummary> getAllTicketsPage(TicketStatus status, TicketPriority priority, SortField sortBy, SortOrder sortOrder, Integer pageNumber, Integer pageSize);

    Ticket getTicket(String ticketId);

    Ticket getTicketByCustomerIdAndTicketId(String userId, String ticketId);

    Page<TicketSummary> getAssignedTickets(String userId, TicketStatus status, TicketPriority priority, SortField sortBy, SortOrder sortOrder, Integer pageNumber, Integer pageSize);

    Page<TicketSummary> getCustomerOwnedTickets(String userId, TicketStatus status, TicketPriority priority, SortField sortBy, SortOrder sortOrder, Integer pageNumber, Integer pageSize);

    // ======================================================
    // TICKET MODIFICATION
    // ======================================================

    Ticket createTicket(TicketCreateRequest ticketCreateRequest);

    Ticket createCustomerTicket(String userId, TicketCreateRequest ticketCreateRequest);

    TicketSummary updateTicket(String userId, String userRole, String ticketId, TicketUpdateRequest ticketUpdateRequest);

    void deleteTicket(String ticketId);

    void deleteTicket(String userId, String ticketId);

    // ======================================================
    // TICKET WORKFLOW
    // ======================================================

    void assignTicket(String ticketId, String agentId);

    void reopenTicket(String ticketId, String reason);

    void reopenTicket(String userId, String ticketId, String reason);

    void updateTicketStatus(String ticketId, @Valid TicketStatus status);

    void updateTicketStatus(String userId, String userRole, String ticketId, @Valid TicketStatus status);

    void updateTicketPriority(String userId, String userRole, String ticketId, @Valid TicketPriority priority);

    // ======================================================
    // TICKET COMMENT
    // ======================================================

    Comment addTicketComment(String ticketId, String userId, String userRole, String body);

    Comment editTicketComment(String userId, String userRole, String ticketId, String commentId, String newCommentBody);

    void deleteTicketComment(String ticketId, String commentId);

    void deleteTicketComment(String userId, String ticketId, String commentId);

}
