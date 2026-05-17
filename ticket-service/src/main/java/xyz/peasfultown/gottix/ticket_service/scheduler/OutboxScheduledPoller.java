package xyz.peasfultown.gottix.ticket_service.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.gottix.ticket_service.config.RabbitMqConfig;
import xyz.peasfultown.gottix.ticket_service.entity.OutboxEntity;
import xyz.peasfultown.gottix.ticket_service.entity.OutboxStatus;
import xyz.peasfultown.gottix.ticket_service.entity.TicketEntity;
import xyz.peasfultown.gottix.ticket_service.repository.OutboxRepository;

import java.util.List;

@Slf4j
@Component
@EnableScheduling
@Transactional
@RequiredArgsConstructor
public class OutboxScheduledPoller {
    private static final String TYPE_ID_HEADER = "__TypeId__";
    private static final String EVENT_TYPE_HEADER = "x-event-type";

    private final OutboxRepository outboxRepo;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objMapper;

    @Scheduled(
            initialDelay = 5000,
            fixedDelay = 5000
    )
    public void sendOutboxPendingTickets() {
        List<OutboxEntity> tickets = outboxRepo.findOldestPendingTickets(10);

        for (OutboxEntity oe : tickets) {
            try {
                Message m = MessageBuilder.withBody(objMapper.writeValueAsBytes(oe.getPayload()))
                        .setHeader(TYPE_ID_HEADER, "Ticket")
                        .setHeader(EVENT_TYPE_HEADER, oe.getEventType().name())
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .build();

                log.info("sending ticket change event: {}", m);
                rabbitTemplate.send(RabbitMqConfig.exchange, RabbitMqConfig.ticket_change_routingKey, m);
                oe.setStatus(OutboxStatus.PROCESSED);
            } catch (JsonProcessingException e) {
                log.error("unable to send ticket change event", e);
                throw new RuntimeException("unable to send ticket change event", e);
            }
        }
    }

    @Scheduled(
            initialDelay = 5000,
            fixedDelay = 5000
    )
    public void sendOutboxPendingComments() {
        List<OutboxEntity> comments = outboxRepo.findOldestPendingComments(10);

        if (comments.isEmpty())
            return;

        for (OutboxEntity oe : comments) {
            try {
                Message m = MessageBuilder.withBody(objMapper.writeValueAsBytes(oe.getPayload()))
                        .setHeader(TYPE_ID_HEADER, "Comment")
                        .setHeader(EVENT_TYPE_HEADER, oe.getEventType().name())
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .build();

                log.info("sending comment change event: {}", m);
                rabbitTemplate.send(RabbitMqConfig.exchange, RabbitMqConfig.ticketComment_change_routingKey, m);
                oe.setStatus(OutboxStatus.PROCESSED);
            } catch (JsonProcessingException e) {
                log.error("unable to send ticket comment change event", e);
                throw new RuntimeException("unable to send ticket comment change event", e);
            }
        }
    }

    @Scheduled(
            initialDelay = 10000,
            fixedDelay = 10000
    )
    public void cleanProcessedEntities() {
        List<OutboxEntity> entities = outboxRepo.findAllProcessed(10);

        if (entities.isEmpty())
            return;

        log.info("cleaning up processed outbox entities");
        outboxRepo.deleteAll(entities);
    }
}
