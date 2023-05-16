package com.navercorp.pinpoint.web.webhook.service;

import com.navercorp.pinpoint.web.webhook.dao.WebhookDao;
import com.navercorp.pinpoint.web.webhook.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.webhook.dao.memory.MemoryWebhookDao;
import com.navercorp.pinpoint.web.webhook.dao.memory.MemoryWebhookSendInfoDao;
import com.navercorp.pinpoint.web.webhook.model.Webhook;
import com.navercorp.pinpoint.web.webhook.model.WebhookSendInfo;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebhookSendInfoServiceImplTest {
    private List<WebhookSendInfo> webhookSendInfoList;
    private List<Webhook> webhookList;

    @BeforeEach
    public void before() {
        webhookSendInfoList = List.of(
                new WebhookSendInfo("0", "1340", "4115234"),
                new WebhookSendInfo("1", "5134", "4115234"),
                new WebhookSendInfo("2", "5134", "4115230")
        );

        webhookList = List.of(
                new Webhook("1340", "webhook", "testUrl", "testApp", "testGroup"),
                new Webhook("1341", "webhook1", "testUrl1", "testApp", "testGroup")
        );

    }

    @Test
    public void insertAndDeleteWebhookSendInfoTest() {
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(mock(WebhookDao.class));
        WebhookSendInfoService webhookSendInfoService = new WebhookSendInfoServiceImpl(webhookSendInfoDao);

        WebhookSendInfo webhookSendInfo = new WebhookSendInfo("0", "1340", "4115234");
        webhookSendInfoService.insertWebhookSendInfo(webhookSendInfo);

        WebhookSendInfo selectedWebhookSendInfo = webhookSendInfoDao.selectWebhookSendInfo("0");

        assertEquals(webhookSendInfo.getRuleId(), selectedWebhookSendInfo.getRuleId());
        assertEquals(webhookSendInfo.getWebhookId(), selectedWebhookSendInfo.getWebhookId());
        assertEquals(webhookSendInfo.getWebhookSendInfoId(), selectedWebhookSendInfo.getWebhookSendInfoId());

        webhookSendInfoService.deleteWebhookSendInfo(webhookSendInfo);
        selectedWebhookSendInfo = webhookSendInfoDao.selectWebhookSendInfo("0");
        assertNull(selectedWebhookSendInfo);
    }

    @Test
    public void updateWebhookSendInfoTest() {
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(mock(WebhookDao.class));
        WebhookSendInfoService webhookSendInfoService = new WebhookSendInfoServiceImpl(webhookSendInfoDao);

        WebhookSendInfo webhookSendInfo = new WebhookSendInfo("0", "1340", "4115234");
        webhookSendInfoService.insertWebhookSendInfo(webhookSendInfo);

        WebhookSendInfo updatewebhookSendInfo = new WebhookSendInfo("0", "1531", "4135");
        webhookSendInfoService.updateWebhookSendInfo(updatewebhookSendInfo);

        WebhookSendInfo selectedWebhookSendInfo = webhookSendInfoDao.selectWebhookSendInfo("0");

        assertNotEquals(webhookSendInfo, selectedWebhookSendInfo);
        assertEquals(updatewebhookSendInfo.getWebhookSendInfoId(), selectedWebhookSendInfo.getWebhookSendInfoId());
        assertEquals(updatewebhookSendInfo.getWebhookId(), selectedWebhookSendInfo.getWebhookId());
        assertEquals(updatewebhookSendInfo.getRuleId(), selectedWebhookSendInfo.getRuleId());
    }

    @Test
    public void deleteAndSelectWebhookSendInfoByWebhookIdTest() {
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(mock(WebhookDao.class));
        WebhookSendInfoService webhookSendInfoService = new WebhookSendInfoServiceImpl(webhookSendInfoDao);

        for (WebhookSendInfo webhookSendInfo : webhookSendInfoList) {
            webhookSendInfoService.insertWebhookSendInfo(webhookSendInfo);
        }

        List<WebhookSendInfo> selectedWebhookSendInfos = webhookSendInfoService.selectWebhookSendInfoByWebhookId("5134");
        assertThat(selectedWebhookSendInfos).hasSize(2);

        webhookSendInfoService.deleteWebhookSendInfoByWebhookId("5134");

        selectedWebhookSendInfos = webhookSendInfoService.selectWebhookSendInfoByWebhookId("5134");
        assertThat(selectedWebhookSendInfos).isEmpty();
    }

    @Test
    public void deleteAndSelectWebhookSendInfoByRuleIdTest() {
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(mock(WebhookDao.class));
        WebhookSendInfoService webhookSendInfoService = new WebhookSendInfoServiceImpl(webhookSendInfoDao);

        for (WebhookSendInfo webhookSendInfo : webhookSendInfoList) {
            webhookSendInfoService.insertWebhookSendInfo(webhookSendInfo);
        }

        List<WebhookSendInfo> selectedWebhookSendInfos = webhookSendInfoService.selectWebhookSendInfoByRuleId("4115234");
        assertThat(selectedWebhookSendInfos).hasSize(2);

        webhookSendInfoService.deleteWebhookSendInfoByRuleId("4115234");

        selectedWebhookSendInfos = webhookSendInfoService.selectWebhookSendInfoByWebhookId("4115234");
        assertThat(selectedWebhookSendInfos).isEmpty();
    }

    @Test
    public void selectWebhookSendInfoByServiceNameTest() {
        WebhookDao webhookDao = mock(WebhookDao.class);
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(webhookDao);
        when(webhookDao.selectWebhookByServiceName("testGroup")).thenReturn(webhookList);
        WebhookSendInfoService webhookSendInfoService = new WebhookSendInfoServiceImpl(webhookSendInfoDao);

        for (WebhookSendInfo webhookSendInfo : webhookSendInfoList) {
            webhookSendInfoService.insertWebhookSendInfo(webhookSendInfo);
        }

        List<WebhookSendInfo> selectedWebhookSendInfos = webhookSendInfoService.selectWebhookSendInfoByServiceName("testGroup");
        assertThat(selectedWebhookSendInfos).hasSize(1);

    }

    @Test
    public void selectWebhookSendInfoByServiceApplicationIdTest() {
        WebhookDao webhookDao = mock(MemoryWebhookDao.class);
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(webhookDao);
        WebhookSendInfoService webhookSendInfoService = new WebhookSendInfoServiceImpl(webhookSendInfoDao);

        for (WebhookSendInfo webhookSendInfo : webhookSendInfoList) {
            webhookSendInfoService.insertWebhookSendInfo(webhookSendInfo);
        }

        when(webhookDao.selectWebhookByApplicationId("testApp")).thenReturn(webhookList);

        List<WebhookSendInfo> selectedWebhookSendInfos = webhookSendInfoService.selectWebhookSendInfoByApplicationId("testApp");

        assertThat(selectedWebhookSendInfos).hasSize(1);
    }
}