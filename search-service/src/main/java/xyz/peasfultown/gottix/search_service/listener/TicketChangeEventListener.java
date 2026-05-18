package xyz.peasfultown.gottix.search_service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import xyz.peasfultown.gottix.search_service.dto.EventType;
import xyz.peasfultown.gottix.search_service.dto.TicketChangeEvent;
import xyz.peasfultown.gottix.search_service.service.SearchService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketChangeEventListener {
    private final SearchService service;

    @RabbitListener(
            queues = "${rabbitmq.queue.ticket-change}",
            messageConverter = "jsonConverter"
    )
    public void handleTicketChangeEvent(TicketChangeEvent event, @Header("x-event-type") EventType eventType) {
        log.info("rabbit listener picket up a ticket {} event: {}", eventType, event);

        switch (eventType) {
            case CREATE -> { service.indexCreateEvent(event); }
            case UPDATE -> { service.indexUpdateEvent(event); }
            case DELETE -> { service.indexDeleteEvent(event); }
        }
    }
}
