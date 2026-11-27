package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.login.basic.PinpointBasicLoginConfig;
import com.navercorp.pinpoint.login.basic.service.BasicLoginService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = BasicLoginActuatorSecurityTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "pinpoint.modules.web.login=basicLogin",
                "web.security.auth.user=user:user-password",
                "web.security.auth.admin=admin:admin-password",
                "web.security.auth.jwt.secretkey=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                "management.server.port=0",
                "management.endpoints.web.base-path=/monitor",
                "management.endpoints.web.path-mapping.health=l7check",
                "management.endpoints.web.exposure.include=health,info,mappings",
        })
class BasicLoginActuatorSecurityTest {

    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int serverPort;

    @Value("${local.management.port}")
    private int managementPort;

    @Autowired
    private BasicLoginService basicLoginService;

    @Test
    void healthIsOpen() throws Exception {
        assertThat(status(managementPort, "/monitor/l7check", null)).isEqualTo(200);
    }

    @Test
    void actuatorRejectsAnonymous() throws Exception {
        assertThat(status(managementPort, "/monitor/info", null)).isNotEqualTo(200);
        assertThat(status(managementPort, "/monitor", null)).isNotEqualTo(200);
    }

    @Test
    void actuatorRejectsUser() throws Exception {
        assertThat(status(managementPort, "/monitor/info", "user")).isEqualTo(403);
        assertThat(status(managementPort, "/monitor/mappings", "user")).isEqualTo(403);
        assertThat(status(managementPort, "/monitor", "user")).isEqualTo(403);
    }

    @Test
    void actuatorAllowsAdmin() throws Exception {
        assertThat(status(managementPort, "/monitor/info", "admin")).isEqualTo(200);
        assertThat(status(managementPort, "/monitor/mappings", "admin")).isEqualTo(200);
    }

    @Test
    void apiAllowsUser() throws Exception {
        assertThat(status(serverPort, "/api/ping", null)).isNotEqualTo(200);
        assertThat(status(serverPort, "/api/ping", "user")).isEqualTo(200);
    }

    private int status(int port, String path, String userId) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (userId != null) {
            Cookie cookie = basicLoginService.createNewCookie(userId);
            request.header("Cookie", cookie.getName() + "=" + cookie.getValue());
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({PinpointBasicLoginConfig.class, PingController.class})
    static class TestConfig {
    }

    @RestController
    static class PingController {
        @GetMapping("/api/ping")
        String ping() {
            return "pong";
        }
    }
}
