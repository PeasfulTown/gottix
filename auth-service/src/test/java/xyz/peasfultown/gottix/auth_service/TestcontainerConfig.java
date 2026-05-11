package xyz.peasfultown.gottix.auth_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainerConfig {
    @Bean
    PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("ecommerce_auth_testdb")
                .withUsername("testuser")
                .withPassword("testpassword");
        return postgres;
    }

    @Bean
    DynamicPropertyRegistrar configureProperties(PostgreSQLContainer postgresContainer) {
        return registry -> {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
            registry.add("spring.datasource.username", postgresContainer::getUsername);
            registry.add("spring.datasource.password", postgresContainer::getPassword);
            registry.add("spring.datasource.name", postgresContainer::getDatabaseName);
        };
    }

    @Profile({ "test", "rabbitmq" })
    @Bean
    RabbitMQContainer rabbitContainer() {
        RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:4-management");
        rabbitMQContainer.withAdminUser("testuser");
        rabbitMQContainer.withAdminPassword("testpassword");
        return rabbitMQContainer;
    }

    @Profile({ "test", "rabbitmq" })
    @Bean
    DynamicPropertyRegistrar propertyRegistrarRabbitMq(RabbitMQContainer rabbitMQContainer) {
        return registry -> {
            registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
            registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
            registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
            registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
        };
    }
}
