package xyz.peasfultown.gottix.ticket_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TicketChangeNotificationEvent {
    private String ticketId;
    private String receiverId;
    private String message;
    private NotificationType type;
}
