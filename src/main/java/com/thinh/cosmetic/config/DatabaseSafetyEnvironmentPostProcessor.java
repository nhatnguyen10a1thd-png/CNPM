package com.thinh.cosmetic.config;

import java.util.Arrays;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/** Checks destructive demo/test configuration before the datasource is created. */
public class DatabaseSafetyEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final Set<String> DESTRUCTIVE_DDL = Set.of("create", "create-drop", "drop", "drop-and-create");
    private static final Pattern POSTGRES_URL = Pattern.compile("^jdbc:postgresql://[^/?#]+/([^/?#]+)(?:\\?([^#]*))?$");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("spring.datasource.hikari.jdbc-url",
                environment.getProperty("spring.datasource.url", ""));
        boolean destructive = Arrays.stream(new String[]{"spring.jpa.hibernate.ddl-auto",
                "spring.jpa.properties.hibernate.hbm2ddl.auto",
                "spring.jpa.properties.jakarta.persistence.schema-generation.database.action"})
                .map(key -> environment.getProperty(key, "").trim().toLowerCase(java.util.Locale.ROOT))
                .anyMatch(DESTRUCTIVE_DDL::contains);
        boolean demo = Arrays.asList(environment.getActiveProfiles()).contains("demo");
        boolean postgresTest = Arrays.asList(environment.getActiveProfiles()).contains("test-postgres");
        if (demo && !url.startsWith("jdbc:h2:mem:")) {
            throw new IllegalStateException("Demo profile requires an isolated in-memory H2 database.");
        }
        if (url.startsWith("jdbc:postgresql:") && (postgresTest || destructive)) {
            if (!isExplicitTestDatabase(url)) {
                throw new IllegalStateException("Destructive PostgreSQL tests require a database name ending in _test.");
            }
        }
    }

    private boolean isExplicitTestDatabase(String url) {
        // Use an explicit host/database URL. Reject alternate pgjdbc database/service settings
        // rather than infer a target from the username, a service file or query text.
        var match = POSTGRES_URL.matcher(url);
        if (!match.matches()) return false;
        try {
            String database = URLDecoder.decode(match.group(1), StandardCharsets.UTF_8);
            String query = match.group(2);
            if (query != null) {
                for (String parameter : query.split("&")) {
                    String key = URLDecoder.decode(parameter.split("=", 2)[0], StandardCharsets.UTF_8);
                    if (Set.of("pgdbname", "dbname", "service").contains(key.toLowerCase(java.util.Locale.ROOT))) {
                        return false;
                    }
                }
            }
            return database.endsWith("_test");
        } catch (IllegalArgumentException malformedUrl) {
            return false;
        }
    }
}
