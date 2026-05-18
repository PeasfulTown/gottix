package xyz.peasfultown.gottix.search_service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import xyz.peasfultown.gottix.search_service.dto.CommentChangeEvent;
import xyz.peasfultown.gottix.search_service.dto.EventType;
import xyz.peasfultown.gottix.search_service.service.SearchService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentChangeEventListener {
    private final SearchService searchService;

    @RabbitListener(
            queues = "${rabbitmq.queue.ticket-comment-change}",
            messageConverter = "jsonConverter"
    )
    public void handleTicketCommentChangeEvent(CommentChangeEvent event, @Header("x-event-type") EventType eventType) {
        log.debug("rabbit listener picket up a comment {} event: {}", eventType, event);

        switch (eventType) {
            case CREATE ->  { searchService.indexCommentCreateEvent(event); }
            case UPDATE ->  { searchService.indexCommentUpdateEvent(event); }
            case DELETE ->  { searchService.indexCommentDeleteEvent(event); }
        }
    }
}
