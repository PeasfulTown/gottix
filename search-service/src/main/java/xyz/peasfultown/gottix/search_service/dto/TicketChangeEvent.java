package xyz.peasfultown.gottix.search_service.dto;

import lombok.*;

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
}
