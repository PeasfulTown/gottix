package xyz.peasfultown.gottix.ticket_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.gottix.ticket_service.service.OutboxService;

@Slf4j
@Component
@EnableScheduling
@Transactional
@RequiredArgsConstructor
public class OutboxScheduledPoller {
    private final OutboxService outboxService;

    @Scheduled(
            initialDelay = 5000,
            fixedDelay = 5000
    )
    public void sendOutboxPendingTickets() {
        outboxService.sendOutboxPendingTickets();
    }

    @Scheduled(
            initialDelay = 5000,
            fixedDelay = 5000
    )
    public void sendOutboxPendingComments() {
        outboxService.sendOutboxPendingComments();
    }

    @Scheduled(
            initialDelay = 10000,
            fixedDelay = 10000
    )
    public void cleanProcessedEntities() {
        outboxService.cleanupProcessedItems();
    }
}
