package xyz.peasfultown.gottix.search_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketChangeEvent {
    private String id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String customerId;
    private String assignedAgentId;
    private Instant createdAt;
    private Instant updatedAt;
}
