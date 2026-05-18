package xyz.peasfultown.gottix.search_service.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.elasticsearch.core.SearchHit;
import xyz.peasfultown.gottix.search_service.entity.TicketDocument;
import xyz.peasfultown.gottix.search_service.model.Ticket;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    Ticket toModel(TicketDocument document);

    default Ticket toModel(SearchHit<TicketDocument> ticketSearchHit) {
        return this.toModel(ticketSearchHit.getContent());
    };

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    default Instant map(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toInstant();
    }
}
