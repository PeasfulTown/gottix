package xyz.peasfultown.gottix.ticket_service.entity.projection;

import java.util.UUID;

public interface TicketEntityIdsOnlyProjection {
    UUID getId();
    UUID getCustomerId();
    UUID getAssignedAgentId();
}
