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
package com.navercorp.pinpoint.alarm.sender;

import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WebhookAlarmSenderTest {

    @Test
    void retriesSendSamePreparedBody() throws IOException {
        List<String> requestBodies = new ArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/alarm", exchange -> {
            requestBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        });
        server.start();

        try {
            String webhookUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/alarm";
            AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
            when(messageFormatter.formatTitle(any(), any(), any())).thenReturn("title");
            when(messageFormatter.formatWebhookBody(any(), any(), any(), anyBoolean())).thenReturn("body");
            WebhookAlarmSender sender = new WebhookAlarmSender(
                    new RestTemplate(), messageFormatter);
            AlarmNotificationChannel channel = new AlarmNotificationChannel();
            channel.setId(10L);
            channel.setMethodType(AlarmMethodType.WEBHOOK);
            channel.setDestination(webhookUrl);
            AlarmDeliveryPayload payload = sender.prepare(
                    new AlarmRuleV2(),
                    channel,
                    AlarmNotificationChannelConfig.empty(),
                    MetricQueryResult.empty());
            AlarmNotificationOutbox delivery = new AlarmNotificationOutbox();
            delivery.setId(9001L);
            delivery.setMethodType(AlarmMethodType.WEBHOOK);

            sender.send(delivery, payload);
            sender.send(delivery, payload);

            assertEquals(List.of(webhookUrl), payload.destinations());
            assertEquals("{\"title\":\"title\",\"body\":\"body\"}", payload.content());
            assertEquals(List.of(payload.content(), payload.content()), requestBodies);
        } finally {
            server.stop(0);
        }
    }

    // Slack and friends put the credential in the path, so only the host may be logged.
    @Test
    void hostDropsEverythingThatCouldCarryACredential() {
        assertEquals("hooks.slack.com",
                WebhookAlarmSender.host("https://hooks.slack.com/services/T000/B000/XXXXXXXX"));
        assertEquals("example.test",
                WebhookAlarmSender.host("https://example.test/hook?token=secret"));
        assertEquals("unparseable", WebhookAlarmSender.host("not a url"));
    }

    @Test
    void validateWebhookUrlAcceptsPublicUrlAndReturnsNormalizedForm() {
        assertEquals("https://hooks.example.com/services/abc",
                WebhookAlarmSender.validateWebhookUrl("https://hooks.example.com/services/abc"));
        assertEquals("https://hooks.example.com/services/abc",
                WebhookAlarmSender.validateWebhookUrl("https://hooks.example.com/services/./abc"));
    }

    /**
     * A host name is not resolved at registration, so an internal host name is accepted here and
     * decided against the host policy when the request is sent.
     */
    @Test
    void validateWebhookUrlAcceptsHostNameWithoutResolving() {
        assertEquals("http://api.internal.example.com/callback",
                WebhookAlarmSender.validateWebhookUrl("http://api.internal.example.com/callback"));
    }

    @Test
    void validateWebhookUrlRejectsNonPublicAndMalformedUrls() {
        List<String> rejected = List.of(
                "http://localhost/webhook",
                "http://localhost./webhook",
                "http://service.local/webhook",
                "http://127.0.0.1/webhook",
                "http://10.0.0.1/webhook",
                "http://172.16.0.1/webhook",
                "http://192.168.0.1/webhook",
                "http://169.254.169.254/latest/meta-data",
                "http://[::1]/webhook",
                "file:///etc/passwd",
                "ftp://8.8.8.8/webhook",
                "https://user:password@8.8.8.8/webhook",
                "https://8.8.8.8/webhook#fragment",
                "https://8.8.8.8:70000/webhook",
                "http:/example.com",
                "example.com",
                "");
        for (String url : rejected) {
            assertThrows(IllegalArgumentException.class,
                    () -> WebhookAlarmSender.validateWebhookUrl(url),
                    "should be rejected: " + url);
        }
    }

    /**
     * The client refuses redirects on purpose, so that an allowed host cannot hand out one
     * pointing at an internal address. That means a 3xx comes back as a response rather than
     * being followed, and RestTemplate's default error handler does not treat it as a failure
     * -- so without an explicit check a webhook answering 302 is recorded as delivered.
     */
    @Test
    void aRedirectIsNotADelivery() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("moved", HttpStatus.FOUND));
        WebhookAlarmSender sender = new WebhookAlarmSender(
                restTemplate, mock(AlarmMessageFormatter.class));
        AlarmNotificationOutbox delivery = new AlarmNotificationOutbox();
        delivery.setId(9003L);
        delivery.setMethodType(AlarmMethodType.WEBHOOK);
        AlarmDeliveryPayload payload =
                AlarmDeliveryPayload.forWebhook("{}", "https://example.test/hook");

        AlarmSendException e = assertThrows(AlarmSendException.class,
                () -> sender.send(delivery, payload));

        assertTrue(e.getMessage().contains("302"), e.getMessage());
        // No cause, which is how the dispatcher tells a permanent failure from a retryable
        // one: a redirect means the stored url is wrong and the next attempt gets the same
        // answer, so it is dead-lettered rather than retried five times.
        assertNull(e.getCause());
    }

    @Test
    void sendRejectsStoredUrlThatIsNotValid() {
        WebhookAlarmSender sender = new WebhookAlarmSender(
                mock(RestTemplate.class), mock(AlarmMessageFormatter.class));
        AlarmNotificationOutbox delivery = new AlarmNotificationOutbox();
        delivery.setId(9002L);
        delivery.setMethodType(AlarmMethodType.WEBHOOK);
        AlarmDeliveryPayload payload = AlarmDeliveryPayload.forWebhook("{}", "not a url");

        assertThrows(AlarmSendException.class, () -> sender.send(delivery, payload));
    }

    @Test
    void prepareUsesCustomTemplatesAndAppliesWebhookFormat() {
        AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        WebhookAlarmSender sender = new WebhookAlarmSender(restTemplate, messageFormatter);
        AlarmRuleV2 rule = new AlarmRuleV2();
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setDestination("https://example.test/hook");
        AlarmNotificationChannelConfig config = new AlarmNotificationChannelConfig(
                "SLACK", "custom-title", "custom-template");
        MetricQueryResult metricResults = MetricQueryResult.empty();
        when(messageFormatter.formatTitle(rule, "custom-title", metricResults)).thenReturn("rendered-title");
        when(messageFormatter.formatWebhookBody(rule, "custom-template", metricResults, true)).thenReturn("rendered-body");

        AlarmDeliveryPayload payload = sender.prepare(rule, channel, config, metricResults);

        assertEquals("{\"text\":\"rendered-title\\nrendered-body\"}", payload.content());
        assertEquals(List.of("https://example.test/hook"), payload.destinations());
        verify(messageFormatter).formatTitle(rule, "custom-title", metricResults);
        verify(messageFormatter).formatWebhookBody(rule, "custom-template", metricResults, true);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void prepareSlackWithoutCustomTitle_sendsOnlyTheBody() {
        AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
        WebhookAlarmSender sender = new WebhookAlarmSender(mock(RestTemplate.class), messageFormatter);
        AlarmRuleV2 rule = new AlarmRuleV2();
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setDestination("https://example.test/hook");
        AlarmNotificationChannelConfig config = new AlarmNotificationChannelConfig("SLACK", null, null);
        MetricQueryResult metricResults = MetricQueryResult.empty();
        when(messageFormatter.formatWebhookBody(rule, null, metricResults, true)).thenReturn("rendered-body");

        AlarmDeliveryPayload payload = sender.prepare(rule, channel, config, metricResults);

        // the default title only repeats the body headline
        assertEquals("{\"text\":\"rendered-body\"}", payload.content());
    }

    @Test
    void prepareDefaultFormat_rendersWithoutMrkdwn() {
        AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
        WebhookAlarmSender sender = new WebhookAlarmSender(mock(RestTemplate.class), messageFormatter);
        AlarmRuleV2 rule = new AlarmRuleV2();
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setDestination("https://example.test/hook");
        AlarmNotificationChannelConfig config = new AlarmNotificationChannelConfig("DEFAULT", null, null);
        MetricQueryResult metricResults = MetricQueryResult.empty();
        when(messageFormatter.formatTitle(rule, null, metricResults)).thenReturn("rendered-title");
        when(messageFormatter.formatWebhookBody(rule, null, metricResults, false)).thenReturn("rendered-body");

        AlarmDeliveryPayload payload = sender.prepare(rule, channel, config, metricResults);

        assertEquals("{\"title\":\"rendered-title\",\"body\":\"rendered-body\"}", payload.content());
        verify(messageFormatter).formatWebhookBody(rule, null, metricResults, false);
    }

    @Test
    void prepareSlackWithCustomTemplate_keepsTheTitle() {
        AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
        WebhookAlarmSender sender = new WebhookAlarmSender(mock(RestTemplate.class), messageFormatter);
        AlarmRuleV2 rule = new AlarmRuleV2();
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setDestination("https://example.test/hook");
        AlarmNotificationChannelConfig config =
                new AlarmNotificationChannelConfig("SLACK", null, "${metrics}");
        MetricQueryResult metricResults = MetricQueryResult.empty();
        when(messageFormatter.formatTitle(rule, null, metricResults)).thenReturn("rendered-title");
        when(messageFormatter.formatWebhookBody(rule, "${metrics}", metricResults, true))
                .thenReturn("rendered-body");

        AlarmDeliveryPayload payload = sender.prepare(rule, channel, config, metricResults);

        // a custom template has no headline of its own, so the title still leads
        assertEquals("{\"text\":\"rendered-title\\nrendered-body\"}", payload.content());
    }
}
