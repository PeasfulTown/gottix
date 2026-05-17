package xyz.peasfultown.gottix.ticket_service.dto;

import lombok.*;

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
}
