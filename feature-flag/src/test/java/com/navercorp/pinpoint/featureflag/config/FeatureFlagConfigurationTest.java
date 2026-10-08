package com.navercorp.pinpoint.featureflag.config;

import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.featureflag.service.properties.FeatureFlagService;
import com.navercorp.pinpoint.featureflag.service.properties.FeatureFlagServiceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureFlagConfigurationTest {
    private static final String DEFAULT_SERVICE = Service.DEFAULT.getServiceName();
    private static final List<String> INVALID_TARGETS = List.of(
            "{application-name: app}",
            "{service-name: service-a}",
            "{service-name: ' ', application-name: app}",
            "{service-name: service-a, application-name: ' '}",
            "' '");

    @Test
    void bindsMixedTargetsAndPreservesDisabledPriority() throws IOException {
        String yaml = """
                features:
                  - name: feature
                    enabled: false
                    enabled-for:
                      - legacy-app
                      - service-name: service-a
                        application-name: app
                      - service-name: DEFAULT
                        application-name: blocked
                    disabled-for:
                      - blocked
                      - service-name: service-a
                        application-name: app
                """;
        try (AnnotationConfigApplicationContext context = context(yaml)) {
            FeatureFlagService service = context.getBean(FeatureFlagServiceFactory.class).get("feature");
            assertTrue(service.isEnabled(DEFAULT_SERVICE, "legacy-app"));
            assertFalse(service.isEnabled("service-a", "legacy-app"));
            assertTrue(service.isDisabled("service-a", "app"));
            assertTrue(service.isDisabled(DEFAULT_SERVICE, "blocked"));
            assertFalse(service.isEnabled(DEFAULT_SERVICE, "other"));
        }
    }

    @Test
    void objectTargetMatchesOnlyItsService() throws IOException {
        String yaml = """
                features:
                  - name: feature
                    enabled-for:
                      - service-name: service-a
                        application-name: app
                      - legacy
                """;
        try (AnnotationConfigApplicationContext context = context(yaml)) {
            FeatureFlagService service = context.getBean(FeatureFlagServiceFactory.class).get("feature");
            assertTrue(service.isEnabled("service-a", "app"));
            assertFalse(service.isEnabled("service-b", "app"));
            assertFalse(service.isEnabled(DEFAULT_SERVICE, "app"));
            assertTrue(service.isEnabled(DEFAULT_SERVICE, "legacy"));
        }
    }

    @Test
    void stringTargetsAreLiteralApplicationNames() throws IOException {
        String yaml = """
                features:
                  - name: feature
                    enabled-for:
                      - 'service-a^app'
                      - 'app\\name'
                """;
        try (AnnotationConfigApplicationContext context = context(yaml)) {
            FeatureFlagService service = context.getBean(FeatureFlagServiceFactory.class).get("feature");
            assertTrue(service.isEnabled(DEFAULT_SERVICE, "service-a^app"));
            assertTrue(service.isEnabled(DEFAULT_SERVICE, "app\\name"));
            assertFalse(service.isEnabled("service-a", "app"));
        }
    }

    @Test
    void mixedDisabledTargetsKeepUnmatchedApplicationsEnabled() throws IOException {
        String yaml = """
                features:
                  - name: feature
                    disabled-for:
                      - legacy
                      - service-name: service-a
                        application-name: app
                """;
        try (AnnotationConfigApplicationContext context = context(yaml)) {
            FeatureFlagService service = context.getBean(FeatureFlagServiceFactory.class).get("feature");
            assertTrue(service.isDisabled(DEFAULT_SERVICE, "legacy"));
            assertTrue(service.isDisabled("service-a", "app"));
            assertTrue(service.isEnabled("service-b", "app"));
            assertTrue(service.isEnabled("service-a", "legacy"));
            assertTrue(service.isEnabled(DEFAULT_SERVICE, "other"));
        }
    }

    @Test
    void ignoresInvalidTargetsButKeepsValidEntries() throws IOException {
        String yaml = """
                features:
                  - name: feature
                    enabled-for:
                      - {application-name: app}
                      - {service-name: service-a}
                      - {service-name: ' ', application-name: app}
                      - {service-name: service-a, application-name: ' '}
                      - ' '
                      - service-name: service-a
                        application-name: valid
                      - legacy
                """;
        try (AnnotationConfigApplicationContext context = context(yaml)) {
            FeatureFlagService service = context.getBean(FeatureFlagServiceFactory.class).get("feature");
            assertTrue(service.isEnabled("service-a", "valid"));
            assertTrue(service.isEnabled(DEFAULT_SERVICE, "legacy"));
            assertFalse(service.isEnabled(DEFAULT_SERVICE, "other"));
            assertFalse(service.isEnabled(null, "app"));
            assertFalse(service.isEnabled(" ", "app"));
            assertFalse(service.isEnabled("service-a", null));
            assertFalse(service.isEnabled("service-a", " "));
            assertFalse(service.isEnabled(DEFAULT_SERVICE, " "));
        }
    }

    @Test
    void invalidEnabledTargetsBehaveLikeAbsentList() throws IOException {
        for (String invalidTarget : INVALID_TARGETS) {
            String yaml = """
                    features:
                      - name: feature
                        enabled-for:
                          - %s
                    """.formatted(invalidTarget);
            try (AnnotationConfigApplicationContext context = context(yaml)) {
                FeatureFlagService service = context.getBean(FeatureFlagServiceFactory.class).get("feature");
                assertTrue(service.isEnabled(DEFAULT_SERVICE, "other"), invalidTarget);
                assertTrue(service.isEnabled("service-a", "other"), invalidTarget);
            }
        }
    }

    @Test
    void invalidDisabledTargetsDoNotChangeEnabledListDefault() throws IOException {
        for (String invalidTarget : INVALID_TARGETS) {
            String yaml = """
                    features:
                      - name: feature
                        enabled-for:
                          - service-name: service-a
                            application-name: app
                        disabled-for:
                          - %s
                    """.formatted(invalidTarget);
            try (AnnotationConfigApplicationContext context = context(yaml)) {
                FeatureFlagService service = context.getBean(FeatureFlagServiceFactory.class).get("feature");
                assertTrue(service.isEnabled("service-a", "app"), invalidTarget);
                assertFalse(service.isEnabled("service-a", "other"), invalidTarget);
                assertFalse(service.isEnabled("service-b", "app"), invalidTarget);
            }
        }
    }

    private static AnnotationConfigApplicationContext context(String yaml) throws IOException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        try {
            for (PropertySource<?> source : new YamlPropertySourceLoader().load("features",
                    new ByteArrayResource(yaml.getBytes(StandardCharsets.UTF_8)))) {
                context.getEnvironment().getPropertySources().addLast(source);
            }
            context.register(FeatureFlagConfiguration.class);
            context.refresh();
            return context;
        } catch (IOException | RuntimeException e) {
            context.close();
            throw e;
        }
    }
}
