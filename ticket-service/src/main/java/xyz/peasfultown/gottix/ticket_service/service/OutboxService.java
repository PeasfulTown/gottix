package xyz.peasfultown.gottix.ticket_service.service;

import xyz.peasfultown.gottix.ticket_service.entity.TicketEntity;

public interface OutboxService {
    void saveTicketToOutbox(TicketEntity te);
}
