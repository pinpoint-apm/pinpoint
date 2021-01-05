package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.alarm.checker.SlowCountToCalleeChecker;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.AlarmCheckerDetectedValue;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.WebhookPayload;
import com.navercorp.pinpoint.batch.common.BatchConfiguration;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired WebhookSender sender;

    @Mock BatchConfiguration batchConfiguration;
    @Mock UserService userService;
    @Mock RestTemplate restTemplate;

    @Test
    public void constructorRequiresNotNullTest() throws Exception {
        try {
            new WebhookSenderImpl(null , userService, restTemplate);
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
            new WebhookSenderImpl(null, userService, null);
            fail();
        } catch (NullPointerException npe) {
            // pass
        }
    }

    @Test
    public void whenWebhookEnableFalseDoNotTriggerWebhook() throws Exception {
        // given
        BatchConfiguration configurationStub = getConfigurationStub(false);
        RestTemplate restTemplateStub = getRestTemplateStubSuccessToSend();
        UserService userGroupServiceStub = getUserServiceStub();

        sender = new WebhookSenderImpl(configurationStub, userGroupServiceStub, restTemplateStub);

        // when
        sender.sendWebhook(null, 0, null);

        // then
        verify(restTemplateStub, never())
                .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    public void whenWebhookEnableTrueDoTriggerWebhook() throws Exception {
        // given
        BatchConfiguration configurationStub = getConfigurationStub(true);
        RestTemplate restTemplateStub = getRestTemplateStubSuccessToSend();
        UserService userGroupServiceStub = getUserServiceStub();

        sender = new WebhookSenderImpl(configurationStub, userGroupServiceStub, restTemplateStub);

        // when
        sender.sendWebhook(getAlarmCheckerStub(), 0, null);

        // then
        verify(restTemplateStub, times(1))
                .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }

    @Test
    public void testTriggerWebhookWithSpringRestTemplateSuccess() throws Exception {
        // given
        BatchConfiguration configurationStub = getConfigurationStub(true);
        RestTemplate restTemplateStub = getRestTemplateStubSuccessToSend();
        UserService userGroupServiceStub = getUserServiceStub();

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

    private UserService getUserServiceStub() {
        UserService mock = mock(UserService.class);
        User member1 = new User();
        User member2 = new User();
        User member3 = new User();

        List<User> testUserGroupMember = Arrays.asList(member1, member2, member3);

        when(mock.selectUserByUserGroupId(USER_GROUP_ID)).thenReturn(testUserGroupMember);

        return mock;
    }

    private AlarmChecker<Long> getAlarmCheckerStub() {
        SlowCountToCalleeChecker checker = mock(SlowCountToCalleeChecker.class);
        when(checker.getCheckerDetectedValue()).thenReturn(new AlarmCheckerDetectedValue<>(100));

        doReturn(new Rule(
                    "app-id",
                    "server-type",
                    "checker-name",
                    0,
                    USER_GROUP_ID,
                    false,
                    false,
                    true,
                    "notes"))
                .when(checker)
                .getRule();

        return checker;
    }
}