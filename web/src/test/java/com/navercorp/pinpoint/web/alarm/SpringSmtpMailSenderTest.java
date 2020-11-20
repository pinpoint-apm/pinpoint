package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.checker.AgentChecker;
import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.checker.ErrorCountChecker;
import com.navercorp.pinpoint.web.alarm.checker.HeapUsageRateChecker;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import com.navercorp.pinpoint.web.service.UserGroupService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class SpringSmtpMailSenderTest {
    
    private static final String MAIL_SENDER_ADDRESS = "pinpoint_operator@pinpoint.com";
    private static final String USER_GROUP_ID = "test-group";
    private static final String EMAIL_MESSAGE = "Example Message";
    private static final String PINPOINT_URL = "https://www.pinpoint.com";
    private static final String BATCH_ENV = "release";
    
    private SpringSmtpMailSender springSmtpMailSender;
    private BatchConfiguration batchConfiguration;
    private UserGroupService userGroupService;
    private JavaMailSenderImpl javaMailSender;
    
    @Before
    public void setup() {
        batchConfiguration = getBatchConfigurationStub();
        userGroupService = getUserGroupServiceStub();
        javaMailSender = getJavaMailSenderStub();
    }
    
    @Test
    public void constructorRequireNonNullTest() {
        try {
            new SpringSmtpMailSender(null, userGroupService, javaMailSender);
            fail("batchConfiguration cannot be null");
        } catch (NullPointerException npe) {
            assertThat(npe.getMessage(), is("batchConfiguration"));
        }
        
        try {
            new SpringSmtpMailSender(batchConfiguration, null, javaMailSender);
            fail("userGroupService cannot be null");
        } catch (NullPointerException npe) {
            assertThat(npe.getMessage(), is("userGroupService"));
        }
        
        try {
            new SpringSmtpMailSender(batchConfiguration, userGroupService, null);
            fail("springMailSender cannot be null");
        } catch (NullPointerException npe) {
            assertThat(npe.getMessage(), is("mailSender"));
        }
    }
    
    @Test(expected = NullPointerException.class)
    public void whenNotConfigSenderEmailAddressConstructorThrowNullPointerException() {
        //given
        BatchConfiguration batchConfiguration = mock(BatchConfiguration.class);
        
        //when
        when(batchConfiguration.getSenderEmailAddress()).thenReturn(null);
        new SpringSmtpMailSender(batchConfiguration, userGroupService, javaMailSender);
    }
    
    @Test(expected = RuntimeException.class)
    public void whenConfigWrongSenderEmailAddressConstructorThrowRuntimeException() {
        //given
        BatchConfiguration batchConfiguration = mock(BatchConfiguration.class);
        
        //when
        when(batchConfiguration.getSenderEmailAddress()).thenReturn("!@#$%");
        new SpringSmtpMailSender(batchConfiguration, userGroupService, javaMailSender);
    }
    
    @Test
    public void whenNonReceiverExistDoNotSendEmail() {
        //given
        userGroupService = mock(UserGroupService.class);
        springSmtpMailSender = new SpringSmtpMailSender(batchConfiguration, userGroupService, javaMailSender);
        
        //when
        when(userGroupService.selectEmailOfMember(USER_GROUP_ID)).thenReturn(Collections.emptyList());
        springSmtpMailSender.sendEmail(getAlarmCheckerStub(), 0, null);
        
        //then
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }
    
    @Test
    public void whenWrongReceiverExistDoNotSendEmail() {
        //given
        userGroupService = mock(UserGroupService.class);
        springSmtpMailSender = new SpringSmtpMailSender(batchConfiguration, userGroupService, javaMailSender);
        
        //when
        when(userGroupService.selectEmailOfMember(USER_GROUP_ID)).thenReturn(getWrongReceivers());
        springSmtpMailSender.sendEmail(getAlarmCheckerStub(), 0, null);
        
        //then
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }
    
    
    @Test
    public void sendEmailWithAlarmChecker() {
        //given
        springSmtpMailSender = new SpringSmtpMailSender(batchConfiguration, userGroupService, javaMailSender);
        
        //when
        springSmtpMailSender.sendEmail(getAlarmCheckerStub(), 0, null);
        
        //then
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
    
    @Test
    public void sendEmailWithAgentChecker() {
        //given
        springSmtpMailSender = new SpringSmtpMailSender(batchConfiguration, userGroupService, javaMailSender);
    
        //when
        springSmtpMailSender.sendEmail(getAgentCheckerStub(), 0, null);
    
        //then
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
    
    
    private BatchConfiguration getBatchConfigurationStub() {
        BatchConfiguration batchConfigurationMock = mock(BatchConfiguration.class);
        
        when(batchConfigurationMock.getSenderEmailAddress()).thenReturn(MAIL_SENDER_ADDRESS);
        when(batchConfigurationMock.getPinpointUrl()).thenReturn(PINPOINT_URL);
        when(batchConfigurationMock.getBatchEnv()).thenReturn(BATCH_ENV);
        
        return batchConfigurationMock;
    }
    
    private UserGroupService getUserGroupServiceStub() {
        UserGroupService userGroupServiceMock = mock(UserGroupService.class);
        String receiver1 = "receiver1@email.com";
        String receiver2 = "receiver2@email.com";
        String receiver3 = "receiver3@email.com";
        List<String> receivers = Arrays.asList(receiver1, receiver2, receiver3);
        
        when(userGroupServiceMock.selectEmailOfMember(USER_GROUP_ID)).thenReturn(receivers);
        
        return userGroupServiceMock;
    }
    
    private JavaMailSenderImpl getJavaMailSenderStub() {
        JavaMailSenderImpl javaMailSenderMock = mock(JavaMailSenderImpl.class);
        
        when(javaMailSenderMock.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        
        return javaMailSenderMock;
    }
    
    private AlarmChecker getAgentCheckerStub() {
        AgentChecker agentCheckerMock = mock(HeapUsageRateChecker.class);
        Map<String, Long> detectedAgents = new HashMap<>();
        detectedAgents.put("test-app", 5L);
        detectedAgents.put("test-app2", 7L);
    
        when(agentCheckerMock.getEmailMessage()).thenReturn(EMAIL_MESSAGE);
        when(agentCheckerMock.getUserGroupId()).thenReturn(USER_GROUP_ID);
        when(agentCheckerMock.getRule()).thenReturn(new Rule("test-app", "tomcat", "HeapUsageRateChecker", 1, USER_GROUP_ID, true, true, "notes"));
        when(agentCheckerMock.getDetectedAgents()).thenReturn(detectedAgents);
        
        return agentCheckerMock;
    }
    
    private AlarmChecker getAlarmCheckerStub() {
        AlarmChecker alarmCheckerMock = mock(ErrorCountChecker.class);
        
        when(alarmCheckerMock.getEmailMessage()).thenReturn(EMAIL_MESSAGE);
        when(alarmCheckerMock.getUserGroupId()).thenReturn(USER_GROUP_ID);
        when(alarmCheckerMock.getRule()).thenReturn(new Rule("test-app", "tomcat", "ErrorCountChecker", 1, USER_GROUP_ID, true, true, "notes"));
        
        return alarmCheckerMock;
    }
    
    private List<String> getWrongReceivers() {
        return Arrays.asList("!@#!@$!@", "!@#!@$!@$", "@#$@#$!");
    }
    
}