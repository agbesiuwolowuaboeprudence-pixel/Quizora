package com.quizora.backend.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fails fast with an actionable message when PostgreSQL is unreachable.
 *
 * <p>Without this check, a missing database surfaces as Hibernate's misleading
 * "Unable to determine Dialect without JDBC metadata" error, which hides the real
 * problem (nothing listening on jdbc:postgresql://...). This listener runs after the
 * environment is prepared but before any JPA/Hibernate beans are created, so it can
 * report the true cause with concrete fixes.</p>
 *
 * <p>Skipped automatically for non-PostgreSQL URLs (e.g. the {@code demo} profile
 * uses in-memory H2, and tests run against H2).</p>
 */
public class DatabasePreflightCheck implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final int CONNECT_TIMEOUT_SECONDS = 3;

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();
        String url = env.getProperty("spring.datasource.url");

        // Only PostgreSQL needs this pre-check; H2 (demo profile/tests) starts in-process.
        if (url == null || !url.startsWith("jdbc:postgresql")) {
            return;
        }

        String username = env.getProperty("spring.datasource.username", "postgres");
        String password = env.getProperty("spring.datasource.password", "");

        try {
            DriverManager.setLoginTimeout(CONNECT_TIMEOUT_SECONDS);
            try (Connection ignored = DriverManager.getConnection(url, username, password)) {
                // connected - hand over to Spring Boot as usual
            }
        } catch (SQLException e) {
            throw new IllegalStateException("""
                    ============================================================
                    QUIZORA STARTUP FAILED: PostgreSQL is not reachable
                    ------------------------------------------------------------
                    JDBC URL : %s
                    Reason   : %s

                    Fix ONE of these:
                      1. Start PostgreSQL:      docker compose up -d
                      2. Run without Postgres:  ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
                         (in-memory H2 database, zero setup)
                      3. Point the app at an existing database via
                         DB_URL / DB_USER / DB_PASSWORD environment variables.
                    ============================================================"""
                    .formatted(url, String.valueOf(e.getMessage()).trim()), e);
        }
    }
}
