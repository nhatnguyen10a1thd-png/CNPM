package com.thinh.cosmetic.config;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseSafetyTest {
    private void check(String profile, String url, String ddl) {
        StandardEnvironment env = new StandardEnvironment();
        env.setActiveProfiles(profile);
        env.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "spring.datasource.url", url, "spring.jpa.hibernate.ddl-auto", ddl)));
        new DatabaseSafetyEnvironmentPostProcessor().postProcessEnvironment(env, new SpringApplication());
    }
    @Test void refusesDestructiveTestsOnDeveloperDatabase() {
        assertThrows(IllegalStateException.class, () -> check("test-postgres", "jdbc:postgresql://localhost:5432/lunea", "create-drop"));
        assertDoesNotThrow(() -> check("test-postgres", "jdbc:postgresql://localhost:15432/lunea_test", "create-drop"));
    }
    @Test void demoNeverTargetsPostgres() {
        assertThrows(IllegalStateException.class, () -> check("demo", "jdbc:postgresql://localhost:5432/lunea", "create-drop"));
        assertDoesNotThrow(() -> check("demo", "jdbc:h2:mem:lunea_demo", "create-drop"));
    }
}
