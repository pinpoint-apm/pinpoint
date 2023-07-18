package com.navercorp.pinpoint.batch.alarm.sender;

import com.navercorp.pinpoint.batch.alarm.AlarmSenderProperties;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.webhook.service.WebhookService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

public class WebhookSenderFactoryBean implements FactoryBean<WebhookSender> {
    private final AlarmSenderProperties alarmSenderProperties;
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final WebhookService webhookService;

    public WebhookSenderFactoryBean(AlarmSenderProperties alarmSenderProperties,
                                    UserService userService,
                                    RestTemplate restTemplate,
                                    WebhookService webhookService) {
        this.alarmSenderProperties = Objects.requireNonNull(alarmSenderProperties, "alarmSenderProperties");
        this.userService = Objects.requireNonNull(userService, "userService");
        this.restTemplate = Objects.requireNonNull(restTemplate, "springRestTemplate");
        this.webhookService = Objects.requireNonNull(webhookService, "webhookService");
    }

    @Override
    public WebhookSender getObject() throws Exception {
        if (!alarmSenderProperties.isWebhookEnable()) {
            return new WebhookSenderEmptyImpl();
        }
        String pinpointUrl = alarmSenderProperties.getPinpointUrl();
        String batchEnv = alarmSenderProperties.getBatchEnv();

        WebhookPayloadFactory webhookPayloadFactory = new WebhookPayloadFactory(pinpointUrl, batchEnv);
        return new WebhookSenderImpl(webhookPayloadFactory, userService, restTemplate, webhookService);
    }

    @Override
    public Class<WebhookSender> getObjectType() {
        return WebhookSender.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
