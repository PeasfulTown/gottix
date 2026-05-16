package xyz.peasfultown.gottix.ticket_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xyz.peasfultown.gottix.ticket_service.entity.OutboxEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {
    @Query(value = """
            SELECT * FROM outbox
            WHERE entity_type = 'TICKET'
                AND status = 'PENDING'
            ORDER BY created_at ASC
            FOR UPDATE SKIP LOCKED
            LIMIT :batchSize
            """, nativeQuery = true)
    List<OutboxEntity> findOldestPendingTickets(int batchSize);

    @Query(value = """
            SELECT * FROM outbox
            WHERE entity_type = 'COMMENT'
                AND status = 'PENDING'
            ORDER BY created_at ASC
            FOR UPDATE SKIP LOCKED
            LIMIT :batchSize
            """, nativeQuery = true)
    List<OutboxEntity> findOldestPendingComments(int batchSize);
}
