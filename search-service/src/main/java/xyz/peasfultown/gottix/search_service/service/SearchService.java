package xyz.peasfultown.gottix.search_service.service;

import org.springframework.stereotype.Service;
import xyz.peasfultown.gottix.search_service.dto.TicketChangeEvent;

public interface SearchService {
    void indexCreateEvent(TicketChangeEvent event);

    void indexUpdateEvent(TicketChangeEvent event);

    void indexDeleteEvent(TicketChangeEvent event);
}
