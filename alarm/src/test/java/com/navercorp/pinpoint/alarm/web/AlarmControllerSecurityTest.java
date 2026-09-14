package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlarmControllerSecurityTest {

    @Mock
    private AlarmRuleService alarmRuleService;

    @Mock
    private AlarmTemplateService templateService;

    @Mock
    private AlarmChannelService channelService;

    private AlarmRuleController ruleController;
    private AlarmTemplateController templateController;
    private AlarmChannelController channelController;

    @BeforeEach
    void setUp() {
        ruleController = new AlarmRuleController(alarmRuleService);
        templateController = new AlarmTemplateController(templateService);
        channelController = new AlarmChannelController(
                channelService, new AlarmNotificationChannelApiMapper(new ObjectMapper()));
    }

    @Test
    void mutatingRuleEndpointsRequireAlarmEditPermission() throws Exception {
        assertAlarmEditPermission(AlarmRuleController.class.getMethod("createRule", String.class, AlarmRuleV2.class));
        assertAlarmEditPermission(AlarmRuleController.class.getMethod("updateRule", Long.class, String.class, AlarmRuleV2.class));
        assertAlarmEditPermission(AlarmRuleController.class.getMethod(
                "deleteRule", Long.class, String.class, String.class));
        assertAlarmEditPermission(AlarmRuleController.class.getMethod(
                "updateEnabled", Long.class, String.class, String.class, java.util.Map.class));
    }

    @Test
    void mutatingTemplateEndpointsRequireAlarmEditPermission() throws Exception {
        assertAlarmEditPermission(AlarmTemplateController.class.getMethod(
                "createTemplate", String.class, String.class, AlarmTemplate.class));
        assertAlarmEditPermission(AlarmTemplateController.class.getMethod(
                "updateTemplate", Long.class, String.class, String.class, AlarmTemplate.class));
        assertAlarmEditPermission(AlarmTemplateController.class.getMethod(
                "deleteTemplate", Long.class, String.class, String.class));
        assertAlarmEditPermission(AlarmTemplateController.class.getMethod(
                "applyTemplate", Long.class, String.class, String.class, String.class));
        assertAlarmEditPermission(AlarmTemplateController.class.getMethod(
                "unapplyTemplate", Long.class, String.class, String.class, String.class));
    }

    @Test
    void mutatingChannelEndpointsRequireAlarmEditPermission() throws Exception {
        assertAlarmEditPermission(AlarmChannelController.class.getMethod(
                "createChannel", String.class, String.class, AlarmNotificationChannelRequest.class));
        assertAlarmEditPermission(AlarmChannelController.class.getMethod(
                "updateChannel", Long.class, String.class, String.class, AlarmNotificationChannelRequest.class));
        assertAlarmEditPermission(AlarmChannelController.class.getMethod(
                "deleteChannel", Long.class, String.class, String.class));
        assertAlarmEditPermission(AlarmChannelController.class.getMethod(
                "linkRuleChannel", Long.class, Long.class, String.class, String.class,
                com.navercorp.pinpoint.alarm.vo.AlarmRuleChannel.class));
        assertAlarmEditPermission(AlarmChannelController.class.getMethod(
                "unlinkRuleChannel", Long.class, Long.class, String.class, String.class));
        assertAlarmEditPermission(AlarmChannelController.class.getMethod(
                "linkTemplateChannel", Long.class, Long.class, String.class, String.class));
        assertAlarmEditPermission(AlarmChannelController.class.getMethod(
                "unlinkTemplateChannel", Long.class, Long.class, String.class, String.class));
    }

    @Test
    void updateRuleDelegatesOwnershipCheckToService() {
        AlarmRuleV2 rule = rule("service", "app-b");

        ruleController.updateRule(1L, "service", rule);

        verify(alarmRuleService).updateRule(1L, rule);
    }

    @Test
    void deleteRuleDelegatesOwnershipCheckToService() {
        ruleController.deleteRule(1L, "service", "app");

        verify(alarmRuleService).deleteRule("service", "app", 1L);
    }

    @Test
    void updateTemplateDelegatesOwnershipCheckToService() {
        AlarmTemplate request = new AlarmTemplate();
        request.setItems(java.util.List.of(new AlarmTemplateItem()));

        templateController.updateTemplate(10L, "service", "", request);

        verify(templateService).updateTemplate(argThat(template ->
                Long.valueOf(10L).equals(template.getId())
                        && "service".equals(template.getServiceName())));
    }

    @Test
    void deleteTemplateDelegatesOwnershipCheckToService() {
        templateController.deleteTemplate(10L, "default", "");

        verify(templateService).deleteTemplate("default", 10L);
    }

    @Test
    void updateChannelDelegatesOwnershipCheckToService() {
        AlarmNotificationChannelRequest request = new AlarmNotificationChannelRequest();

        channelController.updateChannel(4L, "service", "app", request);

        verify(channelService).updateChannel(
                org.mockito.ArgumentMatchers.eq("service"),
                argThat(channel -> Long.valueOf(4L).equals(channel.getId())
                        && "service".equals(channel.getServiceName())));
    }

    @Test
    void linkRuleChannelDelegatesOwnershipCheckToService() {
        AlarmRuleChannel ruleChannel = new AlarmRuleChannel();

        channelController.linkRuleChannel(1L, 4L, "service", "app", ruleChannel);

        verify(channelService).linkRuleChannel("service", "app", ruleChannel);
    }

    @Test
    void linkTemplateChannelDelegatesOwnershipCheckToService() {
        channelController.linkTemplateChannel(10L, 4L, "service", "");

        verify(channelService).linkTemplateChannel("service", 10L, 4L);
    }

    @Test
    void getChannelsByTemplateDelegatesOwnershipCheckToService() {
        channelController.getChannelsByTemplate(10L, "default");

        verify(channelService).getChannelsByTemplateId("default", 10L);
    }

    private void assertAlarmEditPermission(Method method) {
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize, method + " must be protected by @PreAuthorize");
        assertTrue(preAuthorize.value().contains("naverPermissionEvaluator.hasAlarmPermission"));
        assertTrue(preAuthorize.value().contains("PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER"));
    }

    private AlarmRuleV2 rule(String serviceName, String applicationName) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setServiceName(serviceName);
        rule.setApplicationName(applicationName);
        return rule;
    }
}
