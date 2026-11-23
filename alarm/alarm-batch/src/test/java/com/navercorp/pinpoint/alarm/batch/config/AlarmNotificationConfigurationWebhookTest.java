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
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
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

    // The read timeout restarts with every byte, so only a deadline on the whole exchange stops a trickle.
    @Test
    void webhookRestTemplateGivesUpOnAResponseThatNeverEnds() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/trickle", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream body = exchange.getResponseBody()) {
                while (true) {
                    body.write('x');
                    body.flush();
                    Thread.sleep(100);
                }
            } catch (IOException | InterruptedException ignored) {
                // the client hung up
            }
        });
        server.createContext("/webhook", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] ok = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, ok.length);
            exchange.getResponseBody().write(ok);
            exchange.close();
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            // the host policy refuses loopback outright, so the deadline goes on a client without it
            try (CloseableHttpClient client = HttpClients.custom()
                    .addExecInterceptorFirst("exchangeDeadline",
                            AlarmNotificationConfiguration.exchangeDeadline(Duration.ofMillis(500)))
                    .build()) {
                RestTemplate restTemplate = configuration.webhookRestTemplate(client);
                HttpEntity<?> request = new HttpEntity<>("{}", jsonHeaders());

                assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertThrows(RestClientException.class,
                        () -> restTemplate.exchange(URI.create(base + "/trickle"), HttpMethod.POST, request, String.class)));

                // The deadline of an exchange that already finished does not cut a later one on the same connection.
                URI ok = URI.create(base + "/webhook");
                assertEquals("ok", restTemplate.exchange(ok, HttpMethod.POST, request, String.class).getBody());
                Thread.sleep(700);
                assertEquals("ok", restTemplate.exchange(ok, HttpMethod.POST, request, String.class).getBody());
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
