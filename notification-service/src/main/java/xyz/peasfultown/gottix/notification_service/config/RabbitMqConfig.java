package xyz.peasfultown.gottix.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.peasfultown.gottix.notification_service.dto.TicketChangeNotificationEvent;

import java.util.Map;

@Configuration
public class RabbitMqConfig {
    private static String exchange;
    private static String ticket_change_notify_queue;
    private static String ticket_change_notify_routingKey;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue ticket_change_notify_queue() {
        return new Queue(ticket_change_notify_queue);
    }

    @Bean
    public Binding ticket_change_notify_binding(TopicExchange exchange, Queue ticket_change_notify_queue) {
        return BindingBuilder.bind(ticket_change_notify_queue).to(exchange).with(ticket_change_notify_routingKey);
    }

    // ============================================================
    // CONFIGURATION
    // ============================================================

    @Bean
    public RabbitTemplate rabbitTemplate(
            RabbitTemplateConfigurer configurer, ConnectionFactory connectionFactory, TopicExchange exchange) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        configurer.configure(rabbitTemplate, connectionFactory);
        rabbitTemplate.setExchange(exchange.getName());
        rabbitTemplate.setChannelTransacted(true);
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter(DefaultClassMapper classMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = Map.of(
                "NotificationEvent", TicketChangeNotificationEvent.class
        );
        classMapper.setIdClassMapping(idClassMapping);
        return classMapper;
    }

    // ============================================================
    // SETTERS
    // ============================================================

    @Value("${rabbitmq.exchange.main}")
    public void setExchange(
            String exchange) {
        RabbitMqConfig.exchange = exchange;
    }

    @Value("${rabbitmq.queue.ticket-change-notify}")
    public void setTicket_change_notify_queue(
            String ticket_change_notify_queue) {
        RabbitMqConfig.ticket_change_notify_queue = ticket_change_notify_queue;
    }

    @Value("${rabbitmq.routing-key.ticket-change-notify}")
    public void setTicket_change_notify_routingKey(
            String ticket_change_notify_routingKey) {
        RabbitMqConfig.ticket_change_notify_routingKey = ticket_change_notify_routingKey;
    }
}
