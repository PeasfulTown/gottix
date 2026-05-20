package xyz.peasfultown.gottix.notification_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.gottix.notification_service.entity.NotificationEntity;
import xyz.peasfultown.gottix.notification_service.model.Notification;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    Notification toModel(NotificationEntity entity);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    default Instant map(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toInstant();
    }
}
