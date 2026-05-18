package xyz.peasfultown.gottix.search_service.service;

import xyz.peasfultown.gottix.search_service.dto.CommentChangeEvent;
import xyz.peasfultown.gottix.search_service.dto.TicketChangeEvent;
import xyz.peasfultown.gottix.search_service.model.*;

public interface SearchService {

    // ============================================================
    // QUERYING
    // ============================================================

    PagedTicketResponse queryTickets(String search, TicketStatus status, TicketPriority priority, SortField sortBy, SortOrder sortOrder, Integer pageNumber, Integer pageSize);

    PagedTicketResponse queryCustomerTickets(String customerId, String search, TicketStatus status, TicketPriority priority, SortField sortBy, SortOrder sortOrder, Integer pageNumber, Integer pageSize);

    // ============================================================
    // INDEXING EVENT HANDLER METHODS
    // ============================================================

    void indexCreateEvent(TicketChangeEvent event);

    void indexUpdateEvent(TicketChangeEvent event);

    void indexDeleteEvent(TicketChangeEvent event);

    void indexCommentCreateEvent(CommentChangeEvent event);

    void indexCommentUpdateEvent(CommentChangeEvent event);

    void indexCommentDeleteEvent(CommentChangeEvent event);

}
