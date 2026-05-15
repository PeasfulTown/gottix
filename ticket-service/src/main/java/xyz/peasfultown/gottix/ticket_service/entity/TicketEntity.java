package xyz.peasfultown.gottix.ticket_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "status", columnDefinition = "ticket_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPENED;

    @Column(name = "priority", columnDefinition = "ticket_priority", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Builder.Default
    private TicketPriority priority = TicketPriority.LOW;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "assigned_agent_id")
    private UUID assignedAgentId;

    @OneToMany(
            mappedBy = "ticket",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<CommentEntity> comments = new ArrayList<>();

    public enum TicketStatus {
        OPENED, IN_PROGRESS, RESOLVED, CLOSED;

        public static TicketStatus fromValue(String value) {
            for (TicketStatus s : TicketStatus.values())
                if (s.name().equals(value))
                    return s;

            throw new IllegalArgumentException("unknown ticket status value: " + value);
        }
    }

    public enum TicketPriority {
        LOW, MEDIUM, HIGH;

        public static TicketPriority fromValue(String value) {
            for (TicketPriority p : TicketPriority.values())
                if (p.name().equals(value))
                    return p;

            throw new IllegalArgumentException("unknown ticket priority value: " + value);
        }
    }

}
