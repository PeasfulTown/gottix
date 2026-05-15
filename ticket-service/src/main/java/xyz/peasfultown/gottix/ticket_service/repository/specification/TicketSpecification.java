package xyz.peasfultown.gottix.ticket_service.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import xyz.peasfultown.gottix.ticket_service.entity.TicketEntity;

import java.util.UUID;

public class TicketSpecification {
    public static Specification<TicketEntity> hasStatus(
            TicketEntity.TicketStatus status) {
        return (root, query, cb) ->
                status == null
                        ? null
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<TicketEntity> hasPriority(
            TicketEntity.TicketPriority priority) {
        return (root, query, cb) ->
                priority == null
                        ? null
                        : cb.equal(root.get("priority"), priority);
    }

    public static Specification<TicketEntity> hasAssignedAgentId(
            UUID assignedAgentId) {
        return (root, query, cb) ->
            cb.equal(root.get("assignedAgentId"), assignedAgentId);
    }

    public static Specification<TicketEntity> hasCustomerId(
            UUID customerId) {
        return (root, query, cb) ->
                cb.equal(root.get("customerId"), customerId);
    }
}
