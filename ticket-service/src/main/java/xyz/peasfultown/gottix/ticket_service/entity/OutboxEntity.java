package xyz.peasfultown.gottix.ticket_service.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "outbox", schema = "ticket")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OutboxEntity extends BaseEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", columnDefinition = "outbox_entity_type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private EntityType entityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", columnDefinition = "outbox_event_type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private EventType eventType;

    @Column(name = "payload", columnDefinition = "JSONB", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;

    @Column(name = "status", columnDefinition = "outbox_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

}
