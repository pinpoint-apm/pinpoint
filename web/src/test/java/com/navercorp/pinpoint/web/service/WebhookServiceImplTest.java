package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.dao.WebhookDao;
import com.navercorp.pinpoint.web.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.dao.memory.MemoryWebhookDao;
import com.navercorp.pinpoint.web.dao.memory.MemoryWebhookSendInfoDao;
import com.navercorp.pinpoint.web.vo.Webhook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class WebhookServiceImplTest {

    private List<Webhook> webhookList;

    @Before
    public void before() {
        webhookList = new ArrayList<>(2);
        webhookList.add(new Webhook("1340", "webhook", "testUrl", "testApp", "testGroup"));
        webhookList.add(new Webhook("1341", "webhook1", "testUrl1", "testApp", "testGroup1"));
        webhookList.add(new Webhook("1342", "webhook2", "testUrl2", "testApp1", "testGroup1"));
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
        assertEquals(2, selectedWebhooks.size());

        webhookService.deleteWebhookByApplicationId("testApp");

        selectedWebhooks = webhookService.selectWebhookByApplicationId("testApp");
        assertEquals(0, selectedWebhooks.size());
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
        assertEquals(2, selectedWebhooks.size());

        webhookService.deleteWebhookByServiceName("testGroup1");

        selectedWebhooks = webhookService.selectWebhookByServiceName("testGroup1");
        assertEquals(0, selectedWebhooks.size());
    }
}