package xyz.peasfultown.gottix.search_service.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import xyz.peasfultown.gottix.search_service.entity.TicketDocument;

@Repository
public interface TicketRepository extends ElasticsearchRepository<TicketDocument, String> {
}
