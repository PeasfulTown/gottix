package xyz.peasfultown.gottix.ticket_service.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
