package xyz.peasfultown.gottix.ticket_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.gottix.ticket_service.entity.OutboxEntity;
import xyz.peasfultown.gottix.ticket_service.entity.TicketEntity;
import xyz.peasfultown.gottix.ticket_service.repository.OutboxRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OutboxServiceImpl implements OutboxService {
    private final OutboxRepository outboxRepo;
    private final ObjectMapper objMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void saveTicketToOutbox(TicketEntity te) {
        OutboxEntity oe = OutboxEntity.builder()
                .payload(objMapper.valueToTree(te))
                .build();

        oe = outboxRepo.save(oe);
        log.info("saved ticket to outbox: {}", oe);
    }

}
