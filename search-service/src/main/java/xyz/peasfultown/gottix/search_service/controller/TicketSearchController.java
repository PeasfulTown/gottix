package xyz.peasfultown.gottix.search_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.gottix.search_service.TicketApi;
import xyz.peasfultown.gottix.search_service.model.*;
import xyz.peasfultown.gottix.search_service.service.SearchService;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class TicketSearchController implements TicketApi {
    private final SearchService searchService;


    @Override
    public ResponseEntity<PagedTicketResponse> searchTickets(
            String xUserId,
            String xUserRole,
            String search,
            TicketStatus status,
            TicketPriority priority,
            @RequestParam(defaultValue = "CREATED_AT") SortField sortBy,
            @RequestParam(defaultValue = "ASC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize) throws Exception {
        if (xUserRole.equals("ADMIN") || xUserRole.equals("AGENT"))
            return ok(searchService.queryTickets(search, status, priority, sortBy, sortOrder, pageNumber, pageSize));
        else
            return ok(searchService.queryCustomerTickets(xUserId, search, status, priority, sortBy, sortOrder, pageNumber, pageSize));
    }

    @Override
    public ResponseEntity<SearchSuggestion> searchTicketsSuggestion(
            String xUserId,
            String xUserRole,
            String search,
            Integer limit) throws Exception {
        if (xUserRole.equals("ADMIN") || xUserRole.equals("AGENT"))
            return ok(searchService.getSearchSuggestion(search, limit));
        else
            return ok(searchService.getCustomerSearchSuggestion(xUserId, search, limit));
    }
}
