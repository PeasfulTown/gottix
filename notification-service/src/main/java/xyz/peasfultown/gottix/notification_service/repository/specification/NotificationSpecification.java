package xyz.peasfultown.gottix.notification_service.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import xyz.peasfultown.gottix.notification_service.entity.NotificationEntity;

public class NotificationSpecification {
    public static Specification<NotificationEntity> hasIsRead(Boolean isRead) {
        return (root, query, cb) ->
            isRead == null
                ? null
                : cb.equal(root.get("is_read"), isRead);
    }

    public static Specification<NotificationEntity> hasType(NotificationEntity.NotificationType type) {
        return (root, query, cb) ->
                type == null
                    ? null
                    : cb.equal(root.get("type"), type);
    }
}
