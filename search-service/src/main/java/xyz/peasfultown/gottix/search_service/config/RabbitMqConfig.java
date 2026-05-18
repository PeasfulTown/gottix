package xyz.peasfultown.gottix.search_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.peasfultown.gottix.search_service.dto.TicketChangeEvent;

import java.util.Map;

@Configuration
public class RabbitMqConfig {
    public static String exchange;

    public static String ticket_change_queue;
    public static String ticket_change_routingKey;

    public static String ticketComment_change_queue;
    public static String ticketComment_change_routingKey;

    // ============================================================
    // DECLARATIONS
    // ============================================================

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue ticket_change_queue() {
        return new Queue(ticket_change_queue);
    }

    @Bean
    public Binding ticketChangeQueue_binding(TopicExchange exchange, Queue ticket_change_queue) {
        return BindingBuilder.bind(ticket_change_queue).to(exchange).with(ticket_change_routingKey);
    }

    @Bean
    public Queue ticketComment_change_queue() {
        return new Queue(ticketComment_change_queue);
    }

    @Bean
    public Binding ticketCommentChangeQueue_binding(TopicExchange exchange, Queue ticketComment_change_queue) {
        return BindingBuilder.bind(ticketComment_change_queue).to(exchange).with(ticketComment_change_routingKey);
    }

    // ============================================================
    // CONFIGURATION
    // ============================================================

    @Bean
    public RabbitTemplate rabbitTemplate(
            RabbitTemplateConfigurer configurer,
            ConnectionFactory connectionFactory,
            TopicExchange exchange,
            Jackson2JsonMessageConverter jsonConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        configurer.configure(rabbitTemplate, connectionFactory);
        rabbitTemplate.setExchange(exchange.getName());
        rabbitTemplate.setMessageConverter(jsonConverter);
        rabbitTemplate.setChannelTransacted(true);
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter(DefaultClassMapper classMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper);
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = Map.of(
                "Ticket", TicketChangeEvent.class
        );
        classMapper.setIdClassMapping(idClassMapping);
        return classMapper;
    }

    // ============================================================
    // SETTERS
    // ============================================================

    @Value("${rabbitmq.exchange.main}")
    public void setExchange(String exchange) {
        RabbitMqConfig.exchange = exchange;
    }

    @Value("${rabbitmq.queue.ticket-change}")
    public void setTicket_change_queue(String ticket_change_queue) {
        RabbitMqConfig.ticket_change_queue = ticket_change_queue;
    }

    @Value("${rabbitmq.routing-key.ticket-change}")
    public void setTicket_change_routingKey(String ticket_change_routingKey) {
        RabbitMqConfig.ticket_change_routingKey = ticket_change_routingKey;
    }

    @Value("${rabbitmq.queue.ticket-comment-change}")
    public void setTicketComment_change_queue(String ticketComment_change_queue) {
        RabbitMqConfig.ticketComment_change_queue = ticketComment_change_queue;
    }

    @Value("${rabbitmq.routing-key.ticket-comment-change}")
    public void setTicketComment_change_routingKey(String ticketComment_change_routingKey) {
        RabbitMqConfig.ticketComment_change_routingKey = ticketComment_change_routingKey;
    }


}
