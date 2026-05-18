package xyz.peasfultown.gottix.search_service.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.util.ObjectBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;
import xyz.peasfultown.gottix.search_service.dto.TicketChangeEvent;
import xyz.peasfultown.gottix.search_service.entity.TicketDocument;
import xyz.peasfultown.gottix.search_service.entity.TicketPriority;
import xyz.peasfultown.gottix.search_service.entity.TicketStatus;
import xyz.peasfultown.gottix.search_service.mapper.TicketMapper;
import xyz.peasfultown.gottix.search_service.model.*;
import xyz.peasfultown.gottix.search_service.repository.TicketRepository;

import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final ElasticsearchOperations ops;
    private final TicketRepository ticketRepo;
    private final TicketMapper ticketMapper;

    // ============================================================
    // QUERYING
    // ============================================================

    @Override
    public PagedTicketResponse queryTickets(
            String search,
            xyz.peasfultown.gottix.search_service.model.TicketStatus status,
            xyz.peasfultown.gottix.search_service.model.TicketPriority priority,
            SortField sortBy,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize) {
        NativeQueryBuilder nq = NativeQuery.builder();

        if (search != null && !search.isBlank())
            withFullTextSearch(nq, search);

        if (status != null)
            withStatus(nq, status);

        if (priority != null)
            withPriority(nq, priority);

        String sortField = switch (sortBy) {
            case CREATED_AT -> "createdAt";
            case UPDATED_AT -> "updatedAt";
        };

        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                sortOrder == SortOrder.DESC
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending()
        );

        NativeQuery query = nq
                .withPageable(pageable)
                .build();

        SearchHits<TicketDocument> hits = ops.search(query, TicketDocument.class);
        SearchPage<TicketDocument> page = SearchHitSupport.searchPageFor(hits, query.getPageable());

        return buildPagedTicketResponse(page);
    }

    // ============================================================
    // QUERY BUILDER
    // ============================================================

    private void withFullTextSearch(NativeQueryBuilder nq, String queryText) {
        nq.withQuery(q -> q
                .bool(b -> b
                        .should(s -> s
                                .match(mt -> mt
                                        .field("title")
                                        .query(queryText)
                                        .fuzziness("AUTO")))));
    }

    private void withStatus(NativeQueryBuilder nq, xyz.peasfultown.gottix.search_service.model.TicketStatus status) {
        nq.withQuery(q -> q
                .bool(b -> b
                        .filter(f -> f
                                .term(t -> t
                                        .field("status")
                                        .value(status.name())))));
    }

    private void withPriority(NativeQueryBuilder nq, xyz.peasfultown.gottix.search_service.model.TicketPriority priority) {
        nq.withQuery(q -> q
                .bool(b -> b
                        .filter(f -> f
                                .term(t -> t
                                        .field("priority")
                                        .value(priority.name())))));
    }

    // ============================================================
    // INDEXING EVENT HANDLER METHODS
    // ============================================================

    @Override
    public void indexCreateEvent(TicketChangeEvent event) {
        TicketDocument t = TicketDocument.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .status(TicketStatus.valueOf(event.getStatus()))
                .priority(TicketPriority.valueOf(event.getPriority()))
                .customerId(event.getCustomerId())
                .assignedAgentId(event.getAssignedAgentId())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();

        ticketRepo.save(t);
    }

    @Override
    public void indexUpdateEvent(TicketChangeEvent event) {
        TicketDocument td = new TicketDocument();
        td.setId(event.getId());

        if (event.getTitle() != null && !event.getTitle().isBlank())
            td.setTitle(event.getTitle());
        if (event.getDescription() != null && !event.getDescription().isBlank())
            td.setDescription(event.getDescription());
        if (event.getStatus() != null && !event.getStatus().isBlank())
            td.setStatus(TicketStatus.valueOf(event.getStatus()));
        if (event.getPriority() != null && !event.getPriority().isBlank())
            td.setPriority(TicketPriority.valueOf(event.getPriority()));
        if (event.getCustomerId() != null && !event.getCustomerId().isBlank())
            td.setCustomerId(event.getCustomerId());
        if (event.getAssignedAgentId() != null && !event.getAssignedAgentId().isBlank())
            td.setAssignedAgentId(event.getAssignedAgentId());

        td.setUpdatedAt(event.getUpdatedAt());

        UpdateQuery uq = UpdateQuery.builder(event.getId())
                .withDocument(ops.getElasticsearchConverter().mapObject(td))
                .build();

        log.debug("updating document {}", td);
        ops.update(uq, IndexCoordinates.of("tickets"));
    }

    @Override
    public void indexDeleteEvent(TicketChangeEvent event) {
        log.debug("deleting document {}", event);
        try {
            ticketRepo.deleteById(event.getId());
        } catch (Exception e) {
            log.error("unable to delete document {}", event.getId(), e);
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private PagedTicketResponse buildPagedTicketResponse(SearchPage<TicketDocument> page) {
        return PagedTicketResponse.builder()
                .content(page.map(ticketMapper::toModel).getContent())
                .page(ResponsePage.builder()
                        .number(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }

}
