package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationChannelConfigParser;
import com.navercorp.pinpoint.alarm.sender.WebhookAlarmSender;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelBinding;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelOwnerType;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Notification channels and what they are attached to.
 * <p>
 * A channel is a service-wide resource; what varies is the owner it binds to — a
 * standalone rule, or a bundle header that lends the channel to every rule stamped from
 * it. Both bindings live here because the checks they share (the channel belongs to this
 * service, its destination still resolves) are the same on either side.
 */
@Service
public class AlarmChannelService {

    private final AlarmNotificationChannelDao channelDao;
    private final AlarmChannelBindingDao channelBindingDao;
    private final AlarmRuleV2Dao ruleDao;
    private final AlarmNotificationChannelConfigParser channelConfigParser;
    private final AlarmBundleLocks locks;

    public AlarmChannelService(AlarmNotificationChannelDao channelDao,
                               AlarmChannelBindingDao channelBindingDao,
                               AlarmRuleV2Dao ruleDao,
                               AlarmNotificationChannelConfigParser channelConfigParser,
                               AlarmBundleLocks locks) {
        this.channelDao = Objects.requireNonNull(channelDao, "channelDao");
        this.channelBindingDao = Objects.requireNonNull(channelBindingDao, "channelBindingDao");
        this.ruleDao = Objects.requireNonNull(ruleDao, "ruleDao");
        this.channelConfigParser = Objects.requireNonNull(channelConfigParser, "channelConfigParser");
        this.locks = Objects.requireNonNull(locks, "locks");
    }

    // ---- Channel CRUD ----

    public List<AlarmNotificationChannel> getChannelsByServiceName(String serviceName) {
        AlarmOwnerships.verifyServiceName(serviceName);
        return channelDao.selectByServiceName(serviceName);
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public AlarmNotificationChannel createChannel(AlarmNotificationChannel channel) {
        AlarmOwnerships.verifyServiceName(channel.getServiceName());
        validateChannelDestination(channel);
        validateChannelConfig(channel);
        channelDao.insert(channel);
        return channel;
    }

    public AlarmNotificationChannel getChannel(String serviceName, Long id) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmNotificationChannel channel = AlarmOwnerships.requireChannel(channelDao.selectById(id), id);
        AlarmOwnerships.verifyChannel(channel, serviceName);
        return channel;
    }

    public List<AlarmNotificationChannel> getChannelsByRuleId(Long ruleId, AlarmApplication application) {
        AlarmRuleV2 rule = AlarmOwnerships.requireRule(ruleDao.selectRuleById(ruleId), ruleId);
        AlarmOwnerships.verifyRule(rule, application);
        return channelDao.selectByRuleId(ruleId);
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void updateChannel(String serviceName, AlarmNotificationChannel channel) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmNotificationChannel currentChannel = getChannelForUpdate(channel.getId());
        AlarmOwnerships.verifyChannel(currentChannel, serviceName);
        channel.setServiceName(serviceName);
        validateChannelDestination(channel);
        validateChannelConfig(channel);
        channelDao.update(channel);
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void deleteChannel(String serviceName, Long id) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmNotificationChannel currentChannel = getChannelForUpdate(id);
        AlarmOwnerships.verifyChannel(currentChannel, serviceName);
        channelBindingDao.deleteByChannelId(id);
        channelDao.delete(id);
    }

    // User group deletion listeners run after commit, so alarm cleanup needs its own transaction.
    @Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void deleteChannelsByUserGroupId(String userGroupId) {
        if (!StringUtils.hasText(userGroupId)) {
            return;
        }
        List<AlarmNotificationChannel> channels =
                channelDao.selectByUserGroupDestinationForUpdate(userGroupId);
        for (AlarmNotificationChannel channel : channels) {
            channelBindingDao.deleteByChannelId(channel.getId());
            channelDao.delete(channel.getId());
        }
    }

    // ---- Template-Channel mapping ----

    public List<AlarmNotificationChannel> getChannelsByTemplateId(String serviceName, Long templateId) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmTemplate template = locks.requireTemplate(templateId);
        AlarmOwnerships.verifyTemplate(template, serviceName);
        List<Long> channelIds = channelBindingDao.selectByOwner(AlarmChannelOwnerType.TEMPLATE, templateId).stream()
                .map(AlarmChannelBinding::getChannelId)
                .toList();
        if (channelIds.isEmpty()) {
            return List.of();
        }
        Map<Long, AlarmNotificationChannel> channelMap = channelDao.selectByIdsWithUsage(channelIds).stream()
                .collect(Collectors.toMap(AlarmNotificationChannel::getId, Function.identity()));
        return channelIds.stream()
                .map(channelMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void linkTemplateChannel(String serviceName, Long templateId, Long channelId) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmTemplate template = locks.requireTemplateForUpdate(templateId);
        AlarmOwnerships.verifyTemplate(template, serviceName);
        AlarmNotificationChannel channel = getChannelForUpdate(channelId);
        validateChannelService(serviceName, channel);
        validateChannelDestination(channel);
        channelBindingDao.insert(new AlarmChannelBinding(
                AlarmChannelOwnerType.TEMPLATE,
                templateId,
                channelId
        ));
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void unlinkTemplateChannel(String serviceName, Long templateId, Long channelId) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmTemplate template = locks.requireTemplateForUpdate(templateId);
        AlarmOwnerships.verifyTemplate(template, serviceName);
        AlarmNotificationChannel channel = getChannelForUpdate(channelId);
        AlarmOwnerships.verifyChannel(channel, serviceName);
        channelBindingDao.delete(AlarmChannelOwnerType.TEMPLATE, templateId, channelId);
    }

    // ---- Rule-Channel mapping ----

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void linkRuleChannel(String serviceName, String applicationName, AlarmRuleChannel ruleChannel) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmRuleV2 rule = locks.getRuleForUpdate(ruleChannel.getRuleId());
        AlarmOwnerships.verifyRule(rule, serviceName, applicationName);
        if (rule.getTemplateItemId() != null) {
            throw new IllegalArgumentException("Template-linked rule channels cannot be customized: ruleId="
                    + ruleChannel.getRuleId());
        }
        AlarmNotificationChannel channel = getChannelForUpdate(ruleChannel.getChannelId());
        validateChannelService(rule.getServiceName(), channel);
        validateChannelDestination(channel);
        channelBindingDao.insert(new AlarmChannelBinding(
                AlarmChannelOwnerType.RULE,
                ruleChannel.getRuleId(),
                ruleChannel.getChannelId()
        ));
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void unlinkRuleChannel(String serviceName, String applicationName, Long ruleId, Long channelId) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmRuleV2 rule = locks.getRuleForUpdate(ruleId);
        AlarmOwnerships.verifyRule(rule, serviceName, applicationName);
        AlarmNotificationChannel channel = getChannelForUpdate(channelId);
        AlarmOwnerships.verifyChannel(channel, serviceName);
        if (rule.getTemplateItemId() != null) {
            throw new IllegalArgumentException("Template-linked rule channels cannot be customized: ruleId=" + ruleId);
        }
        channelBindingDao.delete(AlarmChannelOwnerType.RULE, ruleId, channelId);
    }

    private AlarmNotificationChannel getChannelForUpdate(Long id) {
        AlarmNotificationChannel channel = channelDao.selectByIdForUpdate(id);
        if (channel == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found: " + id);
        }
        return channel;
    }

    private void validateChannelService(String ownerServiceName, AlarmNotificationChannel channel) {
        AlarmOwnerships.verifyChannel(channel, ownerServiceName);
    }

    private void validateChannelDestination(AlarmNotificationChannel channel) {
        validateUserGroupDestination(channel);
        validateWebhookUrl(channel);
    }

    private void validateChannelConfig(AlarmNotificationChannel channel) {
        channelConfigParser.parse(channel.getMethodType(), channel.getConfig());
    }

    private void validateUserGroupDestination(AlarmNotificationChannel channel) {
        if (!isUserGroupDestination(channel)) {
            return;
        }
        String userGroupId = channel.getDestination();
        if (userGroupId == null || !channelDao.existsUserGroup(userGroupId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User group not found: " + userGroupId);
        }
    }

    private static boolean isUserGroupDestination(AlarmNotificationChannel channel) {
        return AlarmMethodType.EMAIL == channel.getMethodType() || AlarmMethodType.SMS == channel.getMethodType();
    }

    /**
     * Stores the normalized URL so that what is validated here is what gets sent later.
     */
    private static void validateWebhookUrl(AlarmNotificationChannel channel) {
        if (AlarmMethodType.WEBHOOK != channel.getMethodType()) {
            return;
        }
        String url = channel.getDestination();
        if (url == null) {
            throw new IllegalArgumentException("Webhook destination must be a valid HTTP or HTTPS URL");
        }
        channel.setDestination(WebhookAlarmSender.validateWebhookUrl(url));
    }
}
