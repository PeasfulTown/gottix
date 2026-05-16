package xyz.peasfultown.gottix.ticket_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xyz.peasfultown.gottix.ticket_service.entity.CommentEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {
    @Query("""
            SELECT c FROM CommentEntity c WHERE c.ticket.id = :ticketId AND c.id = :commentId
            """)
    Optional<CommentEntity> findByTicketIdAndCommentId(UUID ticketId, UUID commentId);
}
