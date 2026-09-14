package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationChannelConfigParser;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelBinding;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelOwnerType;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleChannel;
import com.navercorp.pinpoint.user.vo.UserGroup;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AlarmChannelServiceTest extends AlarmServiceTestSupport {

    @Test
    void createChannelRejectsInvalidWebhook() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        AlarmChannelService service = newChannelService(channelDao);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createChannel(webhookChannel(null, null))
        );

        assertEquals("Webhook destination must be a valid HTTP or HTTPS URL", exception.getMessage());
        assertEquals(0, channelDao.insertedCount);
    }

    @Test
    void createChannelRejectsMissingUserGroup() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        channelDao.setUserGroupIds("existing-group");
        AlarmChannelService service = newChannelService(channelDao);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createChannel(userGroupChannel(null, AlarmMethodType.EMAIL, "deleted-group"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User group not found: deleted-group", exception.getReason());
        assertEquals(0, channelDao.insertedCount);
    }

    @Test
    void createChannelRejectsMissingServiceName() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        AlarmChannelService service = newChannelService(channelDao);
        AlarmNotificationChannel channel = channel(null);
        channel.setServiceName(" ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createChannel(channel)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("serviceName is required", exception.getReason());
        assertEquals(0, channelDao.insertedCount);
    }

    @Test
    void createChannelRejectsTooLongServiceName() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        AlarmChannelService service = newChannelService(channelDao);
        AlarmNotificationChannel channel = channel(null);
        channel.setServiceName("a".repeat(AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH + 1));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createChannel(channel)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("serviceName is too long", exception.getReason());
        assertEquals(0, channelDao.insertedCount);
    }

    @Test
    void createChannelRejectsInvalidConfigsBeforeInsert() {
        for (InvalidConfig invalid : invalidCreateChannelConfigs()) {
            RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
            AlarmChannelService service = newChannelService(channelDao);
            AlarmNotificationChannel channel = channel(null);
            channel.setConfig(invalid.config());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.createChannel(channel)
            );

            assertEquals(invalid.expectedMessage(), exception.getMessage());
            assertEquals(0, channelDao.insertedCount);
        }
    }

    @Test
    void updateChannelAllowsEnabledLinkedRules() {
        RecordingChannelDao channelDao = new RecordingChannelDao(1, 1);
        AlarmChannelService service = newChannelService(channelDao);

        service.updateChannel(SERVICE_NAME, channel(4L));

        assertEquals(1, channelDao.lockedCount);
        assertEquals(1, channelDao.updatedCount);
    }

    @Test
    void updateChannelAllowsDisabledLinkedRules() {
        RecordingChannelDao channelDao = new RecordingChannelDao(1, 0);
        AlarmChannelService service = newChannelService(channelDao);

        service.updateChannel(SERVICE_NAME, channel(4L));

        assertEquals(1, channelDao.lockedCount);
        assertEquals(1, channelDao.updatedCount);
    }

    @Test
    void updateChannelRejectsMissingChannel() {
        RecordingChannelDao channelDao = new RecordingChannelDao(false, 0, 0);
        AlarmChannelService service = newChannelService(channelDao);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateChannel(SERVICE_NAME, webhookChannel(4L, null))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Channel not found: 4", exception.getReason());
        assertEquals(1, channelDao.lockedCount);
        assertEquals(0, channelDao.updatedCount);
    }

    @Test
    void updateChannelRejectsMissingServiceName() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        AlarmChannelService service = newChannelService(channelDao);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateChannel(" ", channel(4L))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("serviceName is required", exception.getReason());
        assertEquals(0, channelDao.lockedCount);
        assertEquals(0, channelDao.updatedCount);
    }

    @Test
    void updateChannelRejectsInvalidWebhook() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        AlarmChannelService service = newChannelService(channelDao);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateChannel(SERVICE_NAME, webhookChannel(4L, null))
        );

        assertEquals("Webhook destination must be a valid HTTP or HTTPS URL", exception.getMessage());
        assertEquals(1, channelDao.lockedCount);
        assertEquals(0, channelDao.updatedCount);
    }

    @Test
    void updateChannelRejectsMissingUserGroup() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        channelDao.setUserGroupIds("existing-group");
        AlarmChannelService service = newChannelService(channelDao);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateChannel(SERVICE_NAME, userGroupChannel(4L, AlarmMethodType.SMS, "deleted-group"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User group not found: deleted-group", exception.getReason());
        assertEquals(1, channelDao.lockedCount);
        assertEquals(0, channelDao.updatedCount);
    }

    @Test
    void updateChannelRejectsDifferentServiceName() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0, "OTHER");
        AlarmChannelService service = newChannelService(channelDao);

        AlarmResourceNotFoundException exception = assertThrows(
                AlarmResourceNotFoundException.class,
                () -> service.updateChannel(SERVICE_NAME, channel(4L))
        );

        assertEquals("Channel not found", exception.getMessage());
        assertEquals(1, channelDao.lockedCount);
        assertEquals(0, channelDao.updatedCount);
    }

    @Test
    void updateChannelRejectsInvalidConfigsBeforeUpdate() {
        for (InvalidConfig invalid : invalidUpdateChannelConfigs()) {
            RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
            AlarmChannelService service = newChannelService(channelDao);
            AlarmNotificationChannel channel = channel(4L);
            channel.setConfig(invalid.config());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.updateChannel(SERVICE_NAME, channel)
            );

            assertEquals(invalid.expectedMessage(), exception.getMessage());
            assertEquals(1, channelDao.lockedCount);
            assertEquals(0, channelDao.updatedCount);
        }
    }

    @Test
    void deleteChannelDeletesRuleBinding() {
        RecordingChannelDao channelDao = new RecordingChannelDao(1, 0);
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        channelBindingDao.insert(new AlarmChannelBinding(AlarmChannelOwnerType.RULE, 7L, 4L));
        AlarmChannelService service = newChannelService(channelDao, channelBindingDao);

        service.deleteChannel(SERVICE_NAME, 4L);

        assertEquals(1, channelDao.lockedCount);
        assertEquals(List.of(4L), channelBindingDao.deletedChannelIds);
        assertEquals(List.of(4L), channelDao.deletedIds);
    }

    @Test
    void deleteChannelDeletesTemplateBindingWithoutAffectedRules() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0).withTemplateCount(1);
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        channelBindingDao.insert(new AlarmChannelBinding(AlarmChannelOwnerType.TEMPLATE, 10L, 4L));
        AlarmChannelService service = newChannelService(channelDao, channelBindingDao);

        service.deleteChannel(SERVICE_NAME, 4L);

        assertEquals(1, channelDao.lockedCount);
        assertEquals(List.of(4L), channelBindingDao.deletedChannelIds);
        assertEquals(List.of(4L), channelDao.deletedIds);
    }

    @Test
    void getChannelsByServiceNameFiltersMissingUserGroupChannels() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        channelDao.setChannels(List.of(
                userGroupChannel(1L, AlarmMethodType.EMAIL, "existing-group"),
                userGroupChannel(2L, AlarmMethodType.SMS, "deleted-group"),
                webhookChannel(3L, "https://example.com/hook")
        ));
        channelDao.setUserGroupIds("existing-group");
        AlarmChannelService service = newChannelService(channelDao);

        List<AlarmNotificationChannel> channels = service.getChannelsByServiceName(SERVICE_NAME);

        assertEquals(List.of(1L, 3L), channels.stream()
                .map(AlarmNotificationChannel::getId)
                .toList());
    }

    @Test
    void getChannelsByRuleIdFiltersMissingUserGroupChannels() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        channelDao.setChannels(List.of(
                userGroupChannel(1L, AlarmMethodType.EMAIL, "deleted-group"),
                userGroupChannel(2L, AlarmMethodType.SMS, "existing-group")
        ));
        channelDao.setUserGroupIds("existing-group");
        AlarmChannelService service = newChannelService(new RecordingRuleDao(true), channelDao,
                new RecordingChannelBindingDao());

        List<AlarmNotificationChannel> channels = service.getChannelsByRuleId(7L, APPLICATION);

        assertEquals(List.of(2L), channels.stream()
                .map(AlarmNotificationChannel::getId)
                .toList());
    }

    @Test
    void getChannelsByRuleIdRejectsDifferentApplication() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        AlarmChannelService service = newChannelService(ruleDao, channelDao, new RecordingChannelBindingDao());

        AlarmResourceNotFoundException exception = assertThrows(
                AlarmResourceNotFoundException.class,
                () -> service.getChannelsByRuleId(7L, new AlarmApplication(
                        SERVICE_NAME, "other-app", AlarmApplication.TYPE_JAVASCRIPT))
        );

        assertEquals("Rule not found", exception.getMessage());
    }

    @Test
    void deleteChannelAllowsUnusedChannel() {
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        AlarmChannelService service = newChannelService(channelDao);

        service.deleteChannel(SERVICE_NAME, 4L);

        assertEquals(1, channelDao.lockedCount);
        assertEquals(List.of(4L), channelDao.deletedIds);
    }

    @Test
    void deleteChannelRejectsMissingChannel() {
        RecordingChannelDao channelDao = new RecordingChannelDao(false, 0, 0);
        AlarmChannelService service = newChannelService(channelDao);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.deleteChannel(SERVICE_NAME, 4L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Channel not found: 4", exception.getReason());
        assertEquals(1, channelDao.lockedCount);
        assertTrue(channelDao.deletedIds.isEmpty());
    }

    @Test
    void deleteChannelsByUserGroupIdDeletesRuleChannelBindingsAndChannels() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        channelDao.setChannels(List.of(
                userGroupChannel(4L, AlarmMethodType.EMAIL, "deleted-group")
        ));
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(ruleDao, channelDao, channelBindingDao);

        service.deleteChannelsByUserGroupId("deleted-group");

        assertEquals(List.of(4L), channelBindingDao.deletedChannelIds);
        assertTrue(channelBindingDao.deletedOwnerIds.isEmpty());
        assertTrue(ruleDao.deletedIds.isEmpty());
        assertEquals(List.of(4L), channelDao.deletedIds);
    }

    @Test
    void deleteChannelsByUserGroupIdSkipsBlankUserGroupId() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        channelDao.setChannels(List.of(
                userGroupChannel(4L, AlarmMethodType.EMAIL, "deleted-group")
        ));
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(ruleDao, channelDao, channelBindingDao);

        service.deleteChannelsByUserGroupId(" ");

        assertTrue(channelBindingDao.deletedUserGroupDestinations.isEmpty());
        assertTrue(channelBindingDao.deletedOwnerIds.isEmpty());
        assertTrue(channelBindingDao.deletedChannelIds.isEmpty());
        assertTrue(ruleDao.deletedIds.isEmpty());
        assertTrue(channelDao.deletedIds.isEmpty());
    }

    @Test
    void userGroupDeletionListenerDeletesAlarmChannelsByDeletedUserGroup() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        channelDao.setChannels(List.of(
                userGroupChannel(4L, AlarmMethodType.EMAIL, "deleted-group")
        ));
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(ruleDao, channelDao, channelBindingDao);
        AlarmUserGroupDeletionListener listener = new AlarmUserGroupDeletionListener(service);

        listener.onUserGroupDeleted(new UserGroup("1", "deleted-group"));

        assertEquals(List.of(4L), channelBindingDao.deletedChannelIds);
        assertTrue(ruleDao.deletedIds.isEmpty());
        assertEquals(List.of(4L), channelDao.deletedIds);
    }

    // ---- Template channels ----

    @Test
    void getChannelsByTemplateUsesSingleBulkQueryAndPreservesBindingOrder() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        channelDao.setChannels(List.of(channel(2L), channel(1L)));
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        channelBindingDao.insert(new AlarmChannelBinding(AlarmChannelOwnerType.TEMPLATE, 20L, 1L));
        channelBindingDao.insert(new AlarmChannelBinding(AlarmChannelOwnerType.TEMPLATE, 20L, 2L));
        AlarmChannelService service = newChannelService(new RecordingRuleDao(true), templateDao, templateItemDao, channelDao,
                channelBindingDao);

        List<AlarmNotificationChannel> channels = service.getChannelsByTemplateId(SERVICE_NAME, 20L);

        assertEquals(List.of(1L, 2L), channels.stream().map(AlarmNotificationChannel::getId).toList());
        assertEquals(List.of(1L, 2L), channelDao.selectedIdsWithUsage);
        assertEquals(0, channelDao.selectedByIdCount);
    }

    @Test
    void linkTemplateChannelAllowsEnabledLinkedRules() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(new RecordingRuleDao(true), templateDao,
                new RecordingTemplateItemDao(), new RecordingChannelDao(0, 0), channelBindingDao);

        service.linkTemplateChannel(SERVICE_NAME, 20L, 4L);

        assertEquals(List.of(20L), templateDao.lockedIds);
        assertEquals(1, channelBindingDao.inserted.size());
    }

    @Test
    void unlinkTemplateChannelAllowsEnabledLinkedRules() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(new RecordingRuleDao(true), templateDao,
                new RecordingTemplateItemDao(), new RecordingChannelDao(0, 0), channelBindingDao);

        service.unlinkTemplateChannel(SERVICE_NAME, 20L, 4L);

        assertEquals(List.of(20L), templateDao.lockedIds);
        assertEquals(List.of(20L), channelBindingDao.deletedOwnerIds);
    }

    @Test
    void linkTemplateChannelRejectsDifferentServiceOwnership() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, "other-service"));
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(new RecordingRuleDao(true), templateDao,
                new RecordingTemplateItemDao(), new RecordingChannelDao(0, 0), channelBindingDao);

        AlarmResourceNotFoundException exception = assertThrows(
                AlarmResourceNotFoundException.class,
                () -> service.linkTemplateChannel(SERVICE_NAME, 20L, 4L)
        );

        assertEquals("Template not found", exception.getMessage());
        assertTrue(channelBindingDao.inserted.isEmpty());
    }

    @Test
    void linkRuleChannelLocksRuleAndChannelBeforeInsert() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(ruleDao, channelDao, channelBindingDao);

        AlarmRuleChannel ruleChannel = new AlarmRuleChannel();
        ruleChannel.setRuleId(7L);
        ruleChannel.setChannelId(4L);

        service.linkRuleChannel(SERVICE_NAME, APPLICATION_NAME, ruleChannel);

        assertEquals(List.of(7L), ruleDao.lockedIds);
        assertEquals(1, channelDao.lockedCount);
        assertEquals(1, channelBindingDao.inserted.size());
        assertEquals(AlarmChannelOwnerType.RULE, channelBindingDao.inserted.get(0).getOwnerType());
        assertEquals(7L, channelBindingDao.inserted.get(0).getOwnerId());
        assertEquals(4L, channelBindingDao.inserted.get(0).getChannelId());
    }

    @Test
    void linkRuleChannelRejectsTemplateLinkedRule() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withTemplateItemId(10L);
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(ruleDao, channelDao, channelBindingDao);

        AlarmRuleChannel ruleChannel = new AlarmRuleChannel();
        ruleChannel.setRuleId(7L);
        ruleChannel.setChannelId(4L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.linkRuleChannel(SERVICE_NAME, APPLICATION_NAME, ruleChannel)
        );

        assertEquals("Template-linked rule channels cannot be customized: ruleId=7", exception.getMessage());
        assertTrue(channelBindingDao.inserted.isEmpty());
    }

    @Test
    void linkRuleChannelRejectsWrongApplication() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(ruleDao, channelDao, channelBindingDao);

        AlarmRuleChannel ruleChannel = new AlarmRuleChannel();
        ruleChannel.setRuleId(7L);
        ruleChannel.setChannelId(4L);

        AlarmResourceNotFoundException exception = assertThrows(
                AlarmResourceNotFoundException.class,
                () -> service.linkRuleChannel(SERVICE_NAME, "other-app", ruleChannel)
        );

        assertEquals("Rule not found", exception.getMessage());
        assertEquals(List.of(7L), ruleDao.lockedIds);
        assertEquals(0, channelDao.lockedCount);
        assertTrue(channelBindingDao.inserted.isEmpty());
    }

    @Test
    void linkRuleChannelRejectsMissingUserGroupChannel() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        channelDao.setChannel(userGroupChannel(4L, AlarmMethodType.EMAIL, "deleted-group"));
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(ruleDao, channelDao, channelBindingDao);

        AlarmRuleChannel ruleChannel = new AlarmRuleChannel();
        ruleChannel.setRuleId(7L);
        ruleChannel.setChannelId(4L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.linkRuleChannel(SERVICE_NAME, APPLICATION_NAME, ruleChannel)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User group not found: deleted-group", exception.getReason());
        assertEquals(List.of(7L), ruleDao.lockedIds);
        assertEquals(1, channelDao.lockedCount);
        assertTrue(channelBindingDao.inserted.isEmpty());
    }

    @Test
    void unlinkRuleChannelLocksRuleAndChannelBeforeDelete() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingChannelDao channelDao = new RecordingChannelDao(0, 0);
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmChannelService service = newChannelService(ruleDao, channelDao, channelBindingDao);

        service.unlinkRuleChannel(SERVICE_NAME, APPLICATION_NAME, 7L, 4L);

        assertEquals(List.of(7L), ruleDao.lockedIds);
        assertEquals(1, channelDao.lockedCount);
        assertEquals(List.of(7L), channelBindingDao.deletedOwnerIds);
        assertEquals(List.of(4L), channelBindingDao.deletedChannelIds);
    }

    private AlarmChannelService newChannelService(RecordingChannelDao channelDao) {
        return newChannelService(channelDao, new RecordingChannelBindingDao());
    }

    private AlarmChannelService newChannelService(RecordingChannelDao channelDao,
                                                  AlarmChannelBindingDao channelBindingDao) {
        return newChannelService(stub(AlarmRuleV2Dao.class), channelDao, channelBindingDao);
    }

    private AlarmChannelService newChannelService(AlarmRuleV2Dao ruleDao,
                                                  RecordingChannelDao channelDao,
                                                  AlarmChannelBindingDao channelBindingDao) {
        return newChannelService(ruleDao, stub(AlarmTemplateDao.class), stub(AlarmTemplateItemDao.class),
                channelDao, channelBindingDao);
    }

    private AlarmChannelService newChannelService(AlarmRuleV2Dao ruleDao,
                                                  AlarmTemplateDao templateDao,
                                                  AlarmTemplateItemDao templateItemDao,
                                                  RecordingChannelDao channelDao,
                                                  AlarmChannelBindingDao channelBindingDao) {
        return new AlarmChannelService(
                channelDao,
                channelBindingDao,
                ruleDao,
                new AlarmNotificationChannelConfigParser(new ObjectMapper()),
                new AlarmBundleLocks(ruleDao, templateDao, templateItemDao)
        );
    }

    static List<InvalidConfig> invalidCreateChannelConfigs() {
        return List.of(
                new InvalidConfig("{not-json",
                        "notification channel config must be a valid JSON object"),
                new InvalidConfig("[]",
                        "notification channel config must be a JSON object"),
                new InvalidConfig("{\"title\":1}",
                        "notification channel config title must be a string")
        );
    }

    static List<InvalidConfig> invalidUpdateChannelConfigs() {
        return List.of(
                new InvalidConfig("{\"template\":{}}",
                        "notification channel config template must be a string"),
                new InvalidConfig("{\"format\":\"TEAMS\"}",
                        "notification channel config format must be DEFAULT or SLACK")
        );
    }

    record InvalidConfig(String config, String expectedMessage) {
    }
}
