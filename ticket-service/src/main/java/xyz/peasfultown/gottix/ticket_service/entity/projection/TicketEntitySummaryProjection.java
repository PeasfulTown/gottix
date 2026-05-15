package xyz.peasfultown.gottix.ticket_service.entity.projection;

import xyz.peasfultown.gottix.ticket_service.entity.TicketEntity;

import java.time.Instant;
import java.util.UUID;

public interface TicketEntitySummaryProjection {
    UUID getId();
    String getTitle();
    String getDescription();
    TicketEntity.TicketStatus getStatus();
    TicketEntity.TicketPriority getPriority();
    UUID getCustomerId();
    UUID getAssignedAgentId();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
