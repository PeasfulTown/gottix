package xyz.peasfultown.gottix.notification_service.dto;

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

    public enum NotificationType {
        TICKET_CREATED,

        TICKET_ASSIGNED,

        TICKET_STATUS_CHANGED,

        TICKET_RESOLVED,

        TICKET_CLOSED,

        TICKET_REOPENED,

        COMMENT_ADDED;
    }
}
