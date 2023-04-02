package com.navercorp.pinpoint.web.webhook.service;

import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.webhook.dao.WebhookDao;
import com.navercorp.pinpoint.web.webhook.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.webhook.dao.memory.MemoryWebhookDao;
import com.navercorp.pinpoint.web.webhook.dao.memory.MemoryWebhookSendInfoDao;
import com.navercorp.pinpoint.web.webhook.model.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class WebhookServiceImplTest {

    private List<Webhook> webhookList;

    @BeforeEach
    public void before() {
        webhookList = List.of(
                new Webhook("1340", "webhook", "testUrl", "testApp", "testGroup"),
                new Webhook("1341", "webhook1", "testUrl1", "testApp", "testGroup1"),
                new Webhook("1342", "webhook2", "testUrl2", "testApp1", "testGroup1")
        );
    }

    @Test
    public void insertAndDeleteWebhookTest() {
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(mock(WebhookDao.class));
        WebhookDao webhookDao = new MemoryWebhookDao(mock(AlarmDao.class));
        WebhookService webhookService = new WebhookServiceImpl(webhookDao, webhookSendInfoDao);

        Webhook webhook = new Webhook("0", "alias", "url", "applicationId", "groupId");
        webhookService.insertWebhook(webhook);

        Webhook selectedWebhook = webhookDao.selectWebhook("0");

        assertEquals(webhook.getWebhookId(), selectedWebhook.getWebhookId());
        assertEquals(webhook.getAlias(), selectedWebhook.getAlias());
        assertEquals(webhook.getUrl(), selectedWebhook.getUrl());
        assertEquals(webhook.getApplicationId(), selectedWebhook.getApplicationId());
        assertEquals(webhook.getServiceName(), selectedWebhook.getServiceName());

        webhookService.deleteWebhook(webhook);
        selectedWebhook = webhookDao.selectWebhook("0");
        assertNull(selectedWebhook);
    }

    @Test
    public void updateWebhookSendInfoTest() {
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(mock(WebhookDao.class));
        WebhookDao webhookDao = new MemoryWebhookDao(mock(AlarmDao.class));
        WebhookService webhookService = new WebhookServiceImpl(webhookDao, webhookSendInfoDao);

        Webhook webhook = new Webhook("0", "alias", "url", "applicationId", "groupId");
        webhookService.insertWebhook(webhook);

        Webhook updateWebhook = new Webhook("0", "alias1", "url1", "applicationId1", "groupId1");
        webhookService.updateWebhook(updateWebhook);

        Webhook selectedWebhook = webhookDao.selectWebhook("0");

        assertNotEquals(webhook, selectedWebhook);
        assertEquals(updateWebhook.getWebhookId(), selectedWebhook.getWebhookId());
        assertEquals(updateWebhook.getAlias(), selectedWebhook.getAlias());
        assertEquals(updateWebhook.getUrl(), selectedWebhook.getUrl());
        assertEquals(updateWebhook.getApplicationId(), selectedWebhook.getApplicationId());
        assertEquals(updateWebhook.getServiceName(), selectedWebhook.getServiceName());
    }

    @Test
    public void deleteAndSelectWebhookByApplicationIdTest() {
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(mock(WebhookDao.class));
        WebhookDao webhookDao = new MemoryWebhookDao(mock(AlarmDao.class));
        WebhookService webhookService = new WebhookServiceImpl(webhookDao, webhookSendInfoDao);

        for (Webhook webhook : webhookList) {
            webhookService.insertWebhook(webhook);
        }

        List<Webhook> selectedWebhooks = webhookService.selectWebhookByApplicationId("testApp");
        assertThat(selectedWebhooks).hasSize(2);

        webhookService.deleteWebhookByApplicationId("testApp");

        selectedWebhooks = webhookService.selectWebhookByApplicationId("testApp");
        assertThat(selectedWebhooks).isEmpty();
    }


    @Test
    public void selectWebhookByServiceNameTest() {
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(mock(WebhookDao.class));
        WebhookDao webhookDao = new MemoryWebhookDao(mock(AlarmDao.class));
        WebhookService webhookService = new WebhookServiceImpl(webhookDao, webhookSendInfoDao);

        for (Webhook webhook : webhookList) {
            webhookService.insertWebhook(webhook);
        }

        List<Webhook> selectedWebhooks = webhookService.selectWebhookByServiceName("testGroup1");
        assertThat(selectedWebhooks).hasSize(2);

        webhookService.deleteWebhookByServiceName("testGroup1");

        selectedWebhooks = webhookService.selectWebhookByServiceName("testGroup1");
        assertThat(selectedWebhooks).isEmpty();
    }
}