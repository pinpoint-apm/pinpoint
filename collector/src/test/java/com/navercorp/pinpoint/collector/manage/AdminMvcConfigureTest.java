package com.navercorp.pinpoint.collector.manage;

import com.navercorp.pinpoint.collector.manage.controller.AuthInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = AdminMvcConfigureTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "collector.admin.api.rest.active=true",
                "collector.admin.password=secret",
                "management.endpoints.web.base-path=/monitor",
                "management.endpoints.web.path-mapping.health=l7check",
                "management.endpoints.web.exposure.include=health,beans,info",
        })
class AdminMvcConfigureTest {

    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    @Test
    void healthIsOpen() throws Exception {
        assertThat(status("/monitor/l7check")).isEqualTo(200);
    }

    @Test
    void actuatorRequiresPassword() throws Exception {
        assertThat(status("/monitor/beans")).isEqualTo(403);
        assertThat(status("/monitor/beans?password=wrong")).isEqualTo(403);
        assertThat(status("/monitor/info")).isEqualTo(403);
        assertThat(status("/monitor")).isEqualTo(403);

        assertThat(status("/monitor/beans?password=secret")).isEqualTo(200);
    }

    @Test
    void adminRequiresPassword() throws Exception {
        assertThat(status("/admin/ping")).isEqualTo(403);
        assertThat(status("/admin/ping?password=secret")).isEqualTo(200);
    }

    @Test
    void otherPathIsOpen() throws Exception {
        assertThat(status("/ping")).isEqualTo(200);
    }

    private int status(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
        return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({AdminMvcConfigure.class, AuthInterceptor.class, PingController.class})
    static class TestConfig {
        @Bean
        JsonMapper jsonMapper() {
            return JsonMapper.builder().build();
        }
    }

    @RestController
    static class PingController {
        @GetMapping({"/admin/ping", "/ping"})
        String ping() {
            return "pong";
        }
    }
}
