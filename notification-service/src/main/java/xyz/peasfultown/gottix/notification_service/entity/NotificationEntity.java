package xyz.peasfultown.gottix.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification", schema = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class NotificationEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "message", nullable = false, updatable = false)
    private String message;

    @Column(name = "type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private NotificationType type;

    @Column(name = "ticket_id", nullable = false, updatable = false)
    private UUID ticketId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at", nullable = false)
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

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
