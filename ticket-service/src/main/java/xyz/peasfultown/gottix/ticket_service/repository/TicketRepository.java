package xyz.peasfultown.gottix.ticket_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xyz.peasfultown.gottix.ticket_service.entity.TicketEntity;
import xyz.peasfultown.gottix.ticket_service.entity.projection.TicketEntityIdsOnlyProjection;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, UUID>,
        JpaSpecificationExecutor<TicketEntity> {
    @Query("""
            SELECT t.id AS id, t.customerId AS customerId, t.assignedAgentId AS assignedAgentId 
            FROM TicketEntity t WHERE t.id = :ticketId
            """)
    Optional<TicketEntityIdsOnlyProjection> findIdsOnlyById(UUID ticketId);

    @Query("""
            SELECT t FROM TicketEntity t WHERE t.customerId = :userId AND t.id = :ticketId
            """)
    Optional<TicketEntity> findByCustomerIdAndTicketId(UUID userId, UUID ticketId);
}
