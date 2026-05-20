package xyz.peasfultown.gottix.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xyz.peasfultown.gottix.notification_service.entity.NotificationEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID>,
        JpaSpecificationExecutor<NotificationEntity> {
    @Modifying
    @Query("""
            UPDATE NotificationEntity n
            SET n.isRead = TRUE,
                n.readAt = CURRENT_TIMESTAMP
            WHERE n.id IN :ids
            """)
    void markAllAsRead(List<UUID> ids);

    @Modifying
    @Query("""
            UPDATE NotificationEntity n
            SET n.isRead = TRUE,
                n.readAt = CURRENT_TIMESTAMP
            WHERE n.id = :notifId
            """)
    void markAsRead(UUID notifId);
}
