package xyz.peasfultown.gottix.ticket_service.entity.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.gottix.ticket_service.entity.CommentEntity;
import xyz.peasfultown.gottix.ticket_service.model.Comment;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toModel(CommentEntity entity);

    default OffsetDateTime map(
            Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    default Instant map(
            OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toInstant();
    }
}
