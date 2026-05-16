package xyz.peasfultown.gottix.ticket_service.entity.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.gottix.ticket_service.entity.TicketEntity;
import xyz.peasfultown.gottix.ticket_service.entity.projection.TicketEntitySummaryProjection;
import xyz.peasfultown.gottix.ticket_service.model.Ticket;
import xyz.peasfultown.gottix.ticket_service.model.TicketSummary;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    Ticket toModel(TicketEntity entity);

    TicketSummary toModelSummary(TicketEntity te);

    TicketSummary toModelSummary(TicketEntitySummaryProjection entity);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    default Instant map(
            OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toInstant();
    }
}
