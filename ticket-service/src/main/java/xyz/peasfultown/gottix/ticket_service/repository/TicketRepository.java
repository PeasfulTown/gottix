package xyz.peasfultown.gottix.ticket_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.peasfultown.gottix.ticket_service.entity.TicketEntity;

import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {
}
