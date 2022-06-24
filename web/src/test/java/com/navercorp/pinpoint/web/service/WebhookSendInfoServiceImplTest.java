package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.WebhookDao;
import com.navercorp.pinpoint.web.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.dao.memory.MemoryWebhookDao;
import com.navercorp.pinpoint.web.dao.memory.MemoryWebhookSendInfoDao;
import com.navercorp.pinpoint.web.vo.Webhook;
import com.navercorp.pinpoint.web.vo.WebhookSendInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

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
        webhookSendInfoList = new ArrayList<>(3);
        webhookSendInfoList.add(new WebhookSendInfo("0", "1340", "4115234"));
        webhookSendInfoList.add(new WebhookSendInfo("1", "5134", "4115234"));
        webhookSendInfoList.add(new WebhookSendInfo("2", "5134", "4115230"));

        webhookList = new ArrayList<>(2);
        webhookList.add(new Webhook("1340", "webhook", "testUrl", "testApp", "testGroup"));
        webhookList.add(new Webhook("1341", "webhook1", "testUrl1", "testApp", "testGroup"));

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
        assertEquals(2, selectedWebhookSendInfos.size());

        webhookSendInfoService.deleteWebhookSendInfoByWebhookId("5134");

        selectedWebhookSendInfos = webhookSendInfoService.selectWebhookSendInfoByWebhookId("5134");
        assertEquals(0, selectedWebhookSendInfos.size());
    }

    @Test
    public void deleteAndSelectWebhookSendInfoByRuleIdTest() {
        WebhookSendInfoDao webhookSendInfoDao = new MemoryWebhookSendInfoDao(mock(WebhookDao.class));
        WebhookSendInfoService webhookSendInfoService = new WebhookSendInfoServiceImpl(webhookSendInfoDao);

        for (WebhookSendInfo webhookSendInfo : webhookSendInfoList) {
            webhookSendInfoService.insertWebhookSendInfo(webhookSendInfo);
        }

        List<WebhookSendInfo> selectedWebhookSendInfos = webhookSendInfoService.selectWebhookSendInfoByRuleId("4115234");
        assertEquals(2, selectedWebhookSendInfos.size());

        webhookSendInfoService.deleteWebhookSendInfoByRuleId("4115234");

        selectedWebhookSendInfos = webhookSendInfoService.selectWebhookSendInfoByWebhookId("4115234");
        assertEquals(0, selectedWebhookSendInfos.size());
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

        assertEquals(1, selectedWebhookSendInfos.size());

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

        assertEquals(1, selectedWebhookSendInfos.size());
    }
}