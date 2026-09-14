package com.navercorp.pinpoint.alarm.sender;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.common.server.webhook.WebhookUrlValidator;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Snapshots the final webhook request body and URL into the outbox payload.
 */
public class WebhookAlarmSender implements AlarmSender {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final RestTemplate restTemplate;
    private final AlarmMessageFormatter messageFormatter;

    public WebhookAlarmSender(RestTemplate restTemplate, AlarmMessageFormatter messageFormatter) {
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate");
        this.messageFormatter = Objects.requireNonNull(messageFormatter, "messageFormatter");
    }

    @Override
    public AlarmMethodType getMethodType() {
        return AlarmMethodType.WEBHOOK;
    }

    @Override
    public AlarmDeliveryPayload prepare(AlarmRuleV2 rule,
                                        AlarmNotificationChannel channel,
                                        AlarmNotificationChannelConfig channelConfig,
                                        MetricQueryResult metricResults) {
        boolean slack = isSlack(channelConfig.format());
        String title = messageFormatter.formatTitle(rule, channelConfig.title(), metricResults);
        String body = messageFormatter.formatWebhookBody(
                rule, channelConfig.template(), metricResults, slack);
        String content = buildPayload(slack, channelConfig, title, body);
        return AlarmDeliveryPayload.forWebhook(content, channel.getDestination());
    }

    @Override
    public void send(AlarmNotificationOutbox delivery, AlarmDeliveryPayload deliveryPayload) {

        List<String> destinations = deliveryPayload.destinations();
        if (destinations == null || destinations.size() != 1) {
            throw new AlarmSendException(
                    "Webhook delivery must have exactly one destination: delivery_id=" + delivery.getId());
        }
        // re-check what was stored; the actual private-address enforcement happens in WebhookDnsResolver
        final String url;
        try {
            url = WebhookUrlValidator.validateSyntax(destinations.get(0));
        } catch (IllegalArgumentException e) {
            throw new AlarmSendException(
                    "Invalid webhook URL: delivery_id=" + delivery.getId(), e);
        }

        String content = deliveryPayload.content();
        if (content == null) {
            throw new AlarmSendException("Webhook content is null: delivery_id=" + delivery.getId());
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(content, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            logger.info("Sent webhook alarm: delivery_id={}, host={}", delivery.getId(), host(url));
        } catch (Exception e) {
            logger.error("Failed to send webhook alarm: delivery_id={}, host={}", delivery.getId(), host(url), e);
            throw new AlarmSendException("Failed to send webhook alarm: delivery_id=" + delivery.getId(), e);
        }
    }

    /**
     * The host alone, never the whole url: providers such as Slack put the credential in
     * the path, so a logged url is a reusable secret sitting wherever logs are kept.
     */
    static String host(String url) {
        try {
            return URI.create(url).getHost();
        } catch (IllegalArgumentException e) {
            return "unparseable";
        }
    }

    private static boolean isSlack(String format) {
        return "SLACK".equalsIgnoreCase(format);
    }

    private static boolean omitTitle(AlarmNotificationChannelConfig channelConfig) {
        return !StringUtils.hasText(channelConfig.title()) && !StringUtils.hasText(channelConfig.template());
    }

    private String buildPayload(boolean slack, AlarmNotificationChannelConfig channelConfig,
                                String title, String body) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        if (slack) {
            // Slack renders one text block. The default title only repeats the headline of
            // the default body, so it is dropped there; a custom title, or any title in
            // front of a custom template, is the channel's own wording and stays.
            payload.put("text", omitTitle(channelConfig) ? body : title + "\n" + body);
        } else {
            payload.put("title", title);
            payload.put("body", body);
        }
        return payload.toString();
    }

    /**
     * Registration-time validation. Rejects a malformed URL, a non-http(s) scheme, credentials or a
     * fragment in the URL, a loopback/link-local host name, and an IP literal in a non-public range.
     * A host name is not resolved here; whether it may reach a private address is decided against
     * {@link com.navercorp.pinpoint.common.server.webhook.WebhookHostPolicy} when the request is
     * actually sent, see {@link com.navercorp.pinpoint.common.server.webhook.WebhookDnsResolver}.
     *
     * @return the normalized URL to store
     */
    public static String validateWebhookUrl(String url) {
        return WebhookUrlValidator.validate(url);
    }
}
