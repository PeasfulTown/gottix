package xyz.peasfultown.gottix.ticket_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.gottix.ticket_service.dto.OutboxComment;
import xyz.peasfultown.gottix.ticket_service.dto.OutboxTicket;
import xyz.peasfultown.gottix.ticket_service.entity.*;
import xyz.peasfultown.gottix.ticket_service.repository.OutboxRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class OutboxServiceImpl implements OutboxService {
    private final OutboxRepository outboxRepo;
    private final ObjectMapper objMapper;

    @Override
    public void saveTicketToOutbox(TicketEntity te, EventType type) {
        OutboxTicket ot = OutboxTicket.builder()
                .id(te.getId().toString())
                .title(te.getTitle() == null ? null : te.getTitle())
                .description(te.getDescription() == null ? null : te.getDescription())
                .status(te.getStatus() == null ? null : te.getStatus().name())
                .priority(te.getPriority() == null ? null : te.getPriority().name())
                .customerId(te.getCustomerId() == null ? null : te.getCustomerId().toString())
                .assignedAgentId(te.getAssignedAgentId() == null ? null : te.getAssignedAgentId().toString())
                .build();

        OutboxEntity oe = OutboxEntity.builder()
                .entityType(EntityType.TICKET)
                .eventType(type)
                .payload(objMapper.valueToTree(ot))
                .build();

        oe = outboxRepo.save(oe);
        log.info("saved ticket to outbox: {}", oe);
    }

    @Override
    public void saveCommentToOutbox(CommentEntity ce, EventType type) {
        OutboxComment oc = OutboxComment.builder()
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

}
