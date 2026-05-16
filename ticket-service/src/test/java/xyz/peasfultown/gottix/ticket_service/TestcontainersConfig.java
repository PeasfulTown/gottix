package xyz.peasfultown.gottix.ticket_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    // containers

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> db() {
        PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:16-alpine");
        db.withDatabaseName("gottix-test");
        db.withUsername("user");
        db.withPassword("password");
        return db;
    }

    @Bean
    @ServiceConnection
    public RabbitMQContainer rab() {
        RabbitMQContainer rab = new RabbitMQContainer("rabbitmq:4-management-alpine");
        rab.withAdminUser("user");
        rab.withAdminPassword("password");
        return rab;
    }

    // properties

//    @Bean
//    @ServiceConnection
//    public DynamicPropertyRegistrar registerProperties(PostgreSQLContainer<?> db, RabbitMQContainer rab) {
//        return registry -> {
//            registry.add("spring.datasource.url", db::getJdbcUrl);
//            registry.add("spring.datasource.username", db::getUsername);
//            registry.add("spring.datasource.password", db::getPassword);
//            registry.add("spring.datasource.name", db::getDatabaseName);
//
//            registry.add("spring.rabbitmq.host", rab::getHost);
//            registry.add("spring.rabbitmq.port", rab::getAmqpPort);
//            registry.add("spring.rabbitmq.username", rab::getAdminUsername);
//            registry.add("spring.rabbitmq.password", rab::getAdminPassword);
//            registry.add("spring.rabbitmq.virtual-host", () -> "gottix-test");
//        };
//    }

}
