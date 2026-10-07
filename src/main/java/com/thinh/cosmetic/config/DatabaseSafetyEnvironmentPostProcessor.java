package com.thinh.cosmetic.config;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/** Checks destructive demo/test configuration before the datasource is created. */
public class DatabaseSafetyEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("spring.datasource.url", "");
        String ddl = environment.getProperty("spring.jpa.hibernate.ddl-auto", "validate");
        boolean demo = Arrays.asList(environment.getActiveProfiles()).contains("demo");
        boolean postgresTest = Arrays.asList(environment.getActiveProfiles()).contains("test-postgres");
        if (demo && !url.startsWith("jdbc:h2:mem:")) {
            throw new IllegalStateException("Demo profile requires an isolated in-memory H2 database.");
        }
        if (url.startsWith("jdbc:postgresql:") && (postgresTest || ddl.startsWith("create"))) {
            String database = url.substring(url.lastIndexOf('/') + 1).split("\\?", 2)[0];
            if (!database.endsWith("_test")) {
                throw new IllegalStateException("Destructive PostgreSQL tests require a database name ending in _test.");
            }
        }
    }
}
