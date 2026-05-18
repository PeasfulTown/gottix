package xyz.peasfultown.gottix.search_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.peasfultown.gottix.search_service.dto.TicketChangeEvent;
import xyz.peasfultown.gottix.search_service.entity.Ticket;
import xyz.peasfultown.gottix.search_service.entity.TicketPriority;
import xyz.peasfultown.gottix.search_service.entity.TicketStatus;
import xyz.peasfultown.gottix.search_service.repository.TicketRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final TicketRepository ticketRepo;

    @Override
    public void indexCreateEvent(TicketChangeEvent event) {
        Ticket t = Ticket.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .status(TicketStatus.valueOf(event.getStatus()))
                .priority(TicketPriority.valueOf(event.getPriority()))
                .customerId(event.getCustomerId())
                .assignedAgentId(event.getAssignedAgentId())
                .build();

        ticketRepo.save(t);
    }

    @Override
    public void indexUpdateEvent(
            TicketChangeEvent event) {

    }

    @Override
    public void indexDeleteEvent(
            TicketChangeEvent event) {

    }

}
