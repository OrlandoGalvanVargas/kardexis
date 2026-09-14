package com.kardexis.support;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public abstract class PostgresTestContainerBase {

    protected static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("kardexis_test")
                .withUsername("test")
                .withPassword("test");
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("app.jwt.secret",
                () -> "super-secret-key-that-must-be-at-least-32-characters-long-for-testing");
        registry.add("app.jwt.access-token-minutes", () -> "15");
        registry.add("app.jwt.refresh-token-days", () -> "7");
        registry.add("app.cors.allowed-origins[0]", () -> "http://localhost:3000");
    }
}