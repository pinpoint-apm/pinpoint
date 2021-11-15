package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.common.BatchConfiguration;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.service.WebhookService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

public class WebhookSenderFactoryBean implements FactoryBean<WebhookSender> {
    private final BatchConfiguration batchConfiguration;
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final WebhookService webhookService;

    public WebhookSenderFactoryBean(BatchConfiguration batchConfiguration,
                                    UserService userService,
                                    RestTemplate restTemplate,
                                    WebhookService webhookService) {
        this.batchConfiguration = Objects.requireNonNull(batchConfiguration, "batchConfiguration");
        this.userService = Objects.requireNonNull(userService, "userService");
        this.restTemplate = Objects.requireNonNull(restTemplate, "springRestTemplate");
        this.webhookService = Objects.requireNonNull(webhookService, "webhookService");
    }

    @Override
    public WebhookSender getObject() throws Exception {
        if (!batchConfiguration.isWebhookEnable()) {
            return new WebhookSenderEmptyImpl();
        }
        String pinpointUrl = batchConfiguration.getPinpointUrl();
        String batchEnv = batchConfiguration.getBatchEnv();

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
