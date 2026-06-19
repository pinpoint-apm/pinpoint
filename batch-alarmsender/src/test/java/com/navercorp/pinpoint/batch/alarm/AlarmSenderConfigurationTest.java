/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.sender.WebhookSender;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookSenderImpl;
import com.navercorp.pinpoint.web.webhook.service.WebhookService;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(MockitoExtension.class)
class AlarmSenderConfigurationTest {

    @Mock
    WebhookService webhookService;

    @Test
    void webhookRestTemplateRejectsPrivateIpLiteralBeforeConnecting() throws Exception {
        AtomicInteger requestCount = new AtomicInteger();
        HttpServer server = newLoopbackServer(requestCount);
        server.start();

        try {
            AlarmSenderConfiguration configuration = new AlarmSenderConfiguration();
            try (CloseableHttpClient webhookHttpClient = configuration.webhookHttpClient()) {
                RestTemplate webhookRestTemplate = configuration.webhookRestTemplate(new RestTemplate(), webhookHttpClient);
                WebhookSender sender = configuration.webhookSender(
                    new AlarmSenderProperties("http://pinpoint.example.com", "test"),
                    webhookRestTemplate,
                    webhookService
                );
                RestTemplate restTemplate = extractRestTemplate(sender);

                String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/webhook";
                HttpEntity<?> requestEntity = new HttpEntity<>(Map.of("message", "test"), jsonHeaders());

                Throwable thrown = catchThrowable(() -> restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class));
                assertThat(thrown)
                    .isInstanceOf(ResourceAccessException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class);
                assertThat(thrown.getCause())
                    .isInstanceOf(UnknownHostException.class)
                    .hasMessageContaining("Webhook host resolves to a non-public address");
                assertThat(requestCount.get()).isZero();
            }
        } finally {
            server.stop(0);
        }
    }

    private static HttpServer newLoopbackServer(AtomicInteger requestCount) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/webhook", exchange -> {
            requestCount.incrementAndGet();
            byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        return server;
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static RestTemplate extractRestTemplate(WebhookSender sender) throws Exception {
        assertThat(sender).isInstanceOf(WebhookSenderImpl.class);
        Field field = WebhookSenderImpl.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        return (RestTemplate) field.get(sender);
    }
}
