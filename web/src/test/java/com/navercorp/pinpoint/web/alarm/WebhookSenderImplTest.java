package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.checker.SlowCountToCalleeChecker;
import com.navercorp.pinpoint.web.alarm.vo.AlarmCheckerDetectedValue;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.alarm.vo.sender.WebhookPayload;
import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.vo.UserGroupMember;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebhookSenderImplTest {
    
    private static final String USER_GROUP_ID = "TEST-GROUP-ID";
    WebhookSenderImpl sender;

    @Mock BatchConfiguration batchConfiguration;
    @Mock UserGroupService userGroupService;
    
    @Mock RestTemplate restTemplate;

    @Test
    public void constructorRequiresNotNullTest() throws Exception {
        try {
            new WebhookSenderImpl(null , userGroupService,  restTemplate);
            fail();
        } catch(NullPointerException npe) {
            // pass
        }
        try {
            new WebhookSenderImpl(new BatchConfiguration(), null, restTemplate);
            fail();
        } catch (NullPointerException npe) {
            // pass
        }
        try {
            new WebhookSenderImpl(new BatchConfiguration(), userGroupService, restTemplate);
            fail();
        } catch (NullPointerException npe) {
            // pass
        }
        try {
            new WebhookSenderImpl(null, userGroupService, null);
            fail();
        } catch (NullPointerException npe) {
            // pass
        }
    }
    
    @Test
    public void whenWebhookEnableFalseDoNotSendWebhook() throws Exception {
        // given
        BatchConfiguration configurationStub = getConfigurationStub(false);
        RestTemplate restTemplateStub = getRestTemplateStubSuccessToSend();
        UserGroupService userGroupServiceStub = getUserGroupServiceStub();

        sender = new WebhookSenderImpl(configurationStub, userGroupServiceStub, restTemplateStub);

        // when
        sender.sendWebhook(null, 0, null);
        
        // then
        verify(restTemplateStub, never())
                .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }
    
    @Test
    public void whenWebhookEnableTrueDoSendWebhook() throws Exception {
        // given
        BatchConfiguration configurationStub = getConfigurationStub(true);
        RestTemplate restTemplateStub = getRestTemplateStubSuccessToSend();
        UserGroupService userGroupServiceStub = getUserGroupServiceStub();

        sender = new WebhookSenderImpl(configurationStub, userGroupServiceStub, restTemplateStub);
        
        // when
        sender.sendWebhook(getAlarmCheckerStub(), 0, null);
        
        // then
        verify(restTemplateStub, times(1))
                .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
        
    }
    
    @Test
    public void testSendWebhookWithSpringRestTemplateSuccess() throws Exception {
        // given
        BatchConfiguration configurationStub = getConfigurationStub(true);
        RestTemplate restTemplateStub = getRestTemplateStubSuccessToSend();
        UserGroupService userGroupServiceStub = getUserGroupServiceStub();

        sender = new WebhookSenderImpl(configurationStub, userGroupServiceStub, restTemplateStub);
    
        ArgumentCaptor<HttpEntity<WebhookPayload>> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    
        // when
        sender.sendWebhook(getAlarmCheckerStub(), 0, null);
        verify(restTemplateStub)
                .exchange(any(URI.class), any(HttpMethod.class), argumentCaptor.capture(), any(Class.class));
    
        // then
        HttpEntity<WebhookPayload> httpEntity = argumentCaptor.getValue();
        MediaType sentContentType = httpEntity.getHeaders().getContentType();
        WebhookPayload sentPayload = httpEntity.getBody();

        assertThat(sentContentType, is(MediaType.APPLICATION_JSON));
        assertThat(sentPayload != null, is(true));
    }

    // helper methods
    private RestTemplate getRestTemplateStubSuccessToSend() {
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        doReturn(ResponseEntity.of(Optional.of("success")))
                .when(restTemplateMock)
                .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
        return restTemplateMock;
    }

    private BatchConfiguration getConfigurationStub(boolean webhookEnable) {
        BatchConfiguration batchConfigurationMock = mock(BatchConfiguration.class);
        
        when(batchConfigurationMock.isWebhookEnable())
                .thenReturn(webhookEnable);
        when(batchConfigurationMock.getWebhookReceiverUrl())
                .thenReturn("test-url");
        when(batchConfigurationMock.getPinpointUrl())
                .thenReturn("pinpoint-url");
        when(batchConfigurationMock.getBatchEnv())
                .thenReturn("batch-env");
        
        
        return batchConfigurationMock;
    }
    
    private UserGroupService getUserGroupServiceStub() {
        UserGroupService mock = mock(UserGroupService.class);
        UserGroupMember member1 = new UserGroupMember(USER_GROUP_ID, "member1");
        UserGroupMember member2 = new UserGroupMember(USER_GROUP_ID, "member2");
        UserGroupMember member3 = new UserGroupMember(USER_GROUP_ID, "member3");
        List<UserGroupMember> testUserGroupMember = Arrays.asList(member1, member2, member3);

        when(mock.selectMember(USER_GROUP_ID)).thenReturn(testUserGroupMember);

        return mock;
    }
    
    private AlarmChecker<Long> getAlarmCheckerStub() {
        SlowCountToCalleeChecker checker = mock(SlowCountToCalleeChecker.class);
        doReturn(new AlarmCheckerDetectedValue<>("unit", 1000L)).when(checker).getCheckerDetectedValue();

        doReturn(new Rule(
                "app-id",
                "server-type",
                "checker-name",
                0,
                USER_GROUP_ID,
                true,
                true,
                "notes"))
            .when(checker)
            .getRule();

        return checker;
    }

}