package com.quizora.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Startup hardening checks for API keys and secrets.
 *
 * <p>Production secrets are required through environment variables, while demo defaults are
 * isolated to the in-memory demo profile. This runner warns about deployment settings that
 * should be restricted before exposing the API.</p>
 */
@Component
public class StartupSecurityChecks implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupSecurityChecks.class);

    private final Environment env;

    public StartupSecurityChecks(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean demo = Arrays.stream(env.getActiveProfiles()).anyMatch("demo"::equalsIgnoreCase);

        String cors = env.getProperty("quizora.cors.allowed-origins", "*");
        if ("*".equals(cors.trim())) {
            warn("CORS_ORIGINS is '*' - every website can call this API. "
                    + "Set CORS_ORIGINS to your real frontend origin(s), comma-separated.");
        }

        boolean seedEnabled = env.getProperty("quizora.seed.enabled", Boolean.class, false);
        if (seedEnabled && !demo) {
            warn("Demo data seeding is enabled outside the 'demo' profile: "
                    + "well-known accounts (admin@quizora.app / Admin@123 etc.) will exist in this database. "
                    + "Disable quizora.seed.enabled before deploying.");
        }

        log.info("Security startup checks completed (profile(s): {}, demo mode: {})",
                demo ? "demo" : String.join(",", env.getActiveProfiles()), demo);
    }

    private void warn(String message) {
        log.warn("SECURITY WARNING: {}", message);
    }
}
