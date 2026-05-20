package xyz.peasfultown.gottix.notification_service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import xyz.peasfultown.gottix.notification_service.dto.TicketChangeNotificationEvent;
import xyz.peasfultown.gottix.notification_service.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketChangeEventListener {
    private final NotificationService notifService;

    @RabbitListener(
            queues = "${rabbitmq.queue.ticket-change-notify}",
            messageConverter = "jsonConverter"
    )
    public void handleTicketChangeEvent(
            TicketChangeNotificationEvent event) {
        log.debug("rabbit listener picked up a ticket change event: {}", event);
        notifService.createNotification(event);
    }
}
