package xyz.peasfultown.gottix.ticket_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.gottix.ticket_service.config.RabbitMqConfig;
import xyz.peasfultown.gottix.ticket_service.dto.CommentChangeEvent;
import xyz.peasfultown.gottix.ticket_service.dto.TicketChangeEvent;
import xyz.peasfultown.gottix.ticket_service.entity.*;
import xyz.peasfultown.gottix.ticket_service.repository.OutboxRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class OutboxServiceImpl implements OutboxService {
    private static final String TYPE_ID_HEADER = "__TypeId__";
    private static final String EVENT_TYPE_HEADER = "x-event-type";

    private final OutboxRepository outboxRepo;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objMapper;

    @Override
    public List<OutboxEntity> getOldestPendingTickets(
            int batchSize) {
        return outboxRepo.findOldestPendingTickets(batchSize);
    }

    @Override
    public void updateOutboxEventStatus(OutboxEntity oe, OutboxStatus status) {
        oe.setStatus(status);
    }

    @Override
    public void saveTicketToOutbox(TicketEntity te, EventType type) {
        TicketChangeEvent ot = TicketChangeEvent.builder()
                .id(te.getId().toString())
                .title(te.getTitle() == null ? null : te.getTitle())
                .description(te.getDescription() == null ? null : te.getDescription())
                .status(te.getStatus() == null ? null : te.getStatus().name())
                .priority(te.getPriority() == null ? null : te.getPriority().name())
                .customerId(te.getCustomerId() == null ? null : te.getCustomerId().toString())
                .assignedAgentId(te.getAssignedAgentId() == null ? null : te.getAssignedAgentId().toString())
                .createdAt(te.getCreatedAt())
                .updatedAt(te.getUpdatedAt())
                .build();

        OutboxEntity oe = OutboxEntity.builder()
                .entityType(EntityType.TICKET)
                .eventType(type)
                .payload(objMapper.valueToTree(ot))
                .build();

        oe = outboxRepo.save(oe);
        log.debug("saved ticket to outbox: {}", oe);
    }

    @Override
    public void saveCommentToOutbox(CommentEntity ce, EventType type) {
        CommentChangeEvent oc = CommentChangeEvent.builder()
                .id(ce.getId().toString())
                .body(ce.getBody())
                .authorId(ce.getAuthorId().toString())
                .build();

        OutboxEntity oe = OutboxEntity.builder()
                .entityType(EntityType.COMMENT)
                .eventType(type)
                .payload(objMapper.valueToTree(oc))
                .build();

        oe = outboxRepo.save(oe);
        log.info("saved comment to outbox: {}", oe);
    }

    @Override
    public void sendOutboxPendingTickets() {
        List<OutboxEntity> tickets = outboxRepo.findOldestPendingTickets(10);

        for (OutboxEntity oe : tickets) {
            try {
                Message m = MessageBuilder.withBody(objMapper.writeValueAsBytes(oe.getPayload()))
                        .setHeader(TYPE_ID_HEADER, "Ticket")
                        .setHeader(EVENT_TYPE_HEADER, oe.getEventType().name())
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .build();

                log.debug("sending ticket {} event: {}", oe.getEventType().toString(), m);
                rabbitTemplate.send(RabbitMqConfig.exchange, RabbitMqConfig.ticket_change_routingKey, m);
                oe.setStatus(OutboxStatus.PROCESSED);
            } catch (
                    JsonProcessingException e) {
                log.error("unable to send ticket change event", e);
                throw new RuntimeException("unable to send ticket change event", e);
            }
        }

    }

    @Override
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

                log.debug("sending comment {} event: {}", oe.getEventType().toString(), m);
                rabbitTemplate.send(RabbitMqConfig.exchange, RabbitMqConfig.ticketComment_change_routingKey, m);
                oe.setStatus(OutboxStatus.PROCESSED);
            } catch (JsonProcessingException e) {
                log.error("unable to send ticket comment change event", e);
                throw new RuntimeException("unable to send ticket comment change event", e);
            }
        }
    }

    @Override
    public void cleanupProcessedItems() {
        List<OutboxEntity> entities = outboxRepo.findAllProcessed(10);

        if (entities.isEmpty())
            return;

        log.debug("cleaning up processed outbox entities");
        outboxRepo.deleteAll(entities);
    }
}
