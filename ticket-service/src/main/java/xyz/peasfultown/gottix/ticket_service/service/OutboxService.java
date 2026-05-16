package xyz.peasfultown.gottix.ticket_service.service;

import xyz.peasfultown.gottix.ticket_service.entity.CommentEntity;
import xyz.peasfultown.gottix.ticket_service.entity.EventType;
import xyz.peasfultown.gottix.ticket_service.entity.TicketEntity;

public interface OutboxService {
    void saveTicketToOutbox(TicketEntity te, EventType type);

    void saveCommentToOutbox(CommentEntity ce, EventType type);
}
