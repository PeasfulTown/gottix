package xyz.peasfultown.gottix.ticket_service.service;

import xyz.peasfultown.gottix.ticket_service.entity.*;

import java.util.List;

public interface OutboxService {
    List<OutboxEntity> getOldestPendingTickets(int batchSize);

    void updateOutboxEventStatus(OutboxEntity oe, OutboxStatus status);

    void saveTicketToOutbox(TicketEntity te, EventType type);

    void saveCommentToOutbox(CommentEntity ce, EventType type);

    void sendOutboxPendingTickets();

    void sendOutboxPendingComments();

    void cleanupProcessedItems();
}
