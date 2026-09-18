/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.batch.config;

import com.navercorp.pinpoint.common.server.webhook.WebhookHostPolicy;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The webhook client has to refuse a non-public address before it opens a connection, which is
 * the only place that can be checked: the policy runs in the DNS resolver, so nothing earlier
 * in the call knows the addresses the connection will use.
 */
class AlarmNotificationConfigurationWebhookTest {

    private final AlarmNotificationConfiguration configuration = new AlarmNotificationConfiguration();

    @Test
    void webhookRestTemplateRejectsLoopbackBeforeConnecting() throws Exception {
        AtomicInteger requestCount = new AtomicInteger();
        HttpServer server = newLoopbackServer(requestCount);
        server.start();

        try {
            WebhookHostPolicy hostPolicy = configuration.webhookHostPolicy(List.of(), List.of());
            try (CloseableHttpClient webhookHttpClient = configuration.webhookHttpClient(hostPolicy)) {
                RestTemplate webhookRestTemplate = configuration.webhookRestTemplate(webhookHttpClient);
                String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/webhook";
                HttpEntity<?> requestEntity = new HttpEntity<>(Map.of("message", "test"), jsonHeaders());

                ResourceAccessException thrown = assertThrows(ResourceAccessException.class,
                        () -> webhookRestTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class));

                UnknownHostException cause = assertInstanceOf(UnknownHostException.class, thrown.getCause());
                assertTrue(cause.getMessage().contains("Webhook host resolves to a non-public address"),
                        cause.getMessage());
                // Refused at resolution, so the server never saw a request.
                assertEquals(0, requestCount.get());
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
}
