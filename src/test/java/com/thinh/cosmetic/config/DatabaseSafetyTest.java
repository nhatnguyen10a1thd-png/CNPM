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
        env.setActiveProfiles(profile.isBlank() ? "dev" : profile);
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

    @Test void queryParametersCannotDisguiseTheDatabase() {
        assertThrows(IllegalStateException.class, () -> check("test-postgres",
                "jdbc:postgresql://localhost/lunea?ApplicationName=/lunea_test", "create-drop"));
        assertThrows(IllegalStateException.class, () -> check("test-postgres",
                "jdbc:postgresql://localhost/lunea_test?PGDBNAME=lunea", "create-drop"));
        assertThrows(IllegalStateException.class, () -> check("test-postgres",
                "jdbc:postgresql://localhost/lunea_test?dbname=lunea", "create-drop"));
        assertDoesNotThrow(() -> check("test-postgres",
                "jdbc:postgresql://localhost/lunea_test?sslmode=require&ApplicationName=/verification", "create-drop"));
    }

    @Test void rejectsDropAndAmbiguousDatabaseUrls() {
        assertThrows(IllegalStateException.class, () -> check("", "jdbc:postgresql://localhost/lunea", "drop"));
        assertThrows(IllegalStateException.class, () -> check("test-postgres",
                "jdbc:postgresql://localhost/?user=lunea_test", "create-drop"));
        assertThrows(IllegalStateException.class, () -> check("test-postgres",
                "jdbc:postgresql://localhost/lunea%5Fprod", "create-drop"));
        assertDoesNotThrow(() -> check("test-postgres", "jdbc:postgresql://localhost/lunea%5Ftest", "create-drop"));
        assertDoesNotThrow(() -> check("", "jdbc:postgresql://localhost/lunea?currentSchema=lunea", "validate"));
    }

    @Test void hikariAndNativeDdlOverridesAreGuarded() {
        for (String ddlKey : new String[]{"spring.jpa.properties.hibernate.hbm2ddl.auto",
                "spring.jpa.properties.jakarta.persistence.schema-generation.database.action"}) {
            StandardEnvironment env = new StandardEnvironment();
            env.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "spring.datasource.url", "jdbc:postgresql://localhost/lunea",
                    "spring.jpa.hibernate.ddl-auto", "validate", ddlKey, "drop")));
            assertThrows(IllegalStateException.class, () -> new DatabaseSafetyEnvironmentPostProcessor()
                    .postProcessEnvironment(env, new SpringApplication()));
        }
        StandardEnvironment env = new StandardEnvironment();
        env.setActiveProfiles("test-postgres");
        env.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "spring.datasource.url", "jdbc:postgresql://localhost/lunea_test",
                "spring.datasource.hikari.jdbc-url", "jdbc:postgresql://localhost/lunea",
                "spring.jpa.hibernate.ddl-auto", "create-drop")));
        assertThrows(IllegalStateException.class, () -> new DatabaseSafetyEnvironmentPostProcessor()
                .postProcessEnvironment(env, new SpringApplication()));
    }
}
