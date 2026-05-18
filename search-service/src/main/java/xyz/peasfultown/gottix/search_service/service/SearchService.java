package xyz.peasfultown.gottix.search_service.service;

import xyz.peasfultown.gottix.search_service.dto.TicketChangeEvent;
import xyz.peasfultown.gottix.search_service.model.*;

public interface SearchService {

    PagedTicketResponse queryTickets(String search, TicketStatus status, TicketPriority priority, SortField sortBy, SortOrder sortOrder, Integer pageNumber, Integer pageSize);

    // ============================================================
    // EVENTS
    // ============================================================

    void indexCreateEvent(TicketChangeEvent event);

    void indexUpdateEvent(TicketChangeEvent event);

    void indexDeleteEvent(TicketChangeEvent event);

}
