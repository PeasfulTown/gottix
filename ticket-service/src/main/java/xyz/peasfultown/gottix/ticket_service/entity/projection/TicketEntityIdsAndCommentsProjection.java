package xyz.peasfultown.gottix.ticket_service.entity.projection;

import xyz.peasfultown.gottix.ticket_service.entity.CommentEntity;

import java.util.List;
import java.util.UUID;

public interface TicketEntityIdsAndCommentsProjection {
    UUID getId();
    UUID getCustomerId();
    List<CommentEntity> getComments();
}
