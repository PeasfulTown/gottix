package xyz.peasfultown.gottix.ticket_service.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static String exchange;
    public static String ticket_update_queue;
    public static String ticket_update_routingKey;


}
