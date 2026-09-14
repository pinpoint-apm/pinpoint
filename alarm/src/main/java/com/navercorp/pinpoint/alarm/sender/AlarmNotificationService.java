package com.navercorp.pinpoint.alarm.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelBinding;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelOwnerType;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.navercorp.pinpoint.alarm.util.ExceptionMessageUtils.rootCauseMessage;

/**
 * Creates immutable notification snapshots and delivers claimed outbox rows.
 * No external send occurs while an alarm evaluation transaction is open.
 */
@Service
public class AlarmNotificationService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AlarmChannelBindingDao channelBindingDao;
    private final AlarmNotificationChannelDao channelDao;
    private final Map<AlarmMethodType, AlarmSender> senderMap;
    private final AlarmNotificationChannelConfigParser channelConfigParser;
    private final ObjectReader payloadReader;
    private final ObjectWriter payloadWriter;

    public AlarmNotificationService(AlarmChannelBindingDao channelBindingDao,
                                    AlarmNotificationChannelDao channelDao,
                                    List<AlarmSender> senders,
                                    ObjectMapper objectMapper,
                                    AlarmNotificationChannelConfigParser channelConfigParser) {
        this.channelBindingDao = Objects.requireNonNull(channelBindingDao, "channelBindingDao");
        this.channelDao = Objects.requireNonNull(channelDao, "channelDao");
        ObjectMapper requiredObjectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.channelConfigParser = Objects.requireNonNull(channelConfigParser, "channelConfigParser");
        this.payloadReader = requiredObjectMapper.readerFor(AlarmDeliveryPayload.class);
        this.payloadWriter = requiredObjectMapper.writerFor(AlarmDeliveryPayload.class);
        this.senderMap = senders.stream()
                .collect(Collectors.toMap(AlarmSender::getMethodType, Function.identity()));
    }

    public PreparationResult prepareNotifications(AlarmRuleV2 rule,
                                                  MetricQueryResult metricResults) {
        AlarmChannelOwnerType ownerType = rule.getTemplateId() == null
                ? AlarmChannelOwnerType.RULE
                : AlarmChannelOwnerType.TEMPLATE;
        Long ownerId = rule.getTemplateId() == null ? rule.getId() : rule.getTemplateId();
        List<AlarmChannelBinding> bindings = channelBindingDao.selectByOwner(ownerType, ownerId);
        if (bindings.isEmpty()) {
            String skippedReason = "No notification channel binding found for "
                    + ownerType + " owner: owner_id=" + ownerId;
            logger.warn("{}; rule_id={}, rule_name={}", skippedReason, rule.getId(), rule.getName());
            return new PreparationResult(List.of(), skippedReason);
        }

        List<Long> channelIds = bindings.stream()
                .map(AlarmChannelBinding::getChannelId)
                .toList();
        Map<Long, AlarmNotificationChannel> channelMap = channelDao.selectByIds(channelIds).stream()
                .collect(Collectors.toMap(AlarmNotificationChannel::getId, Function.identity()));

        List<PreparedDelivery> deliveries = new ArrayList<>(bindings.size());
        List<PreparationFailure> failures = new ArrayList<>();
        for (AlarmChannelBinding binding : bindings) {
            AlarmNotificationChannel channel = channelMap.get(binding.getChannelId());
            if (channel == null) {
                String message = "Channel not found: channel_id=" + binding.getChannelId();
                logger.error("Failed to prepare notification: rule_id={}, channel_id={}, reason={}",
                        rule.getId(), binding.getChannelId(), message);
                failures.add(new PreparationFailure(binding.getChannelId(), message));
                continue;
            }
            AlarmSender sender = senderMap.get(channel.getMethodType());
            if (sender == null) {
                String message = "No sender for method_type: " + channel.getMethodType();
                logger.error("Failed to prepare notification: rule_id={}, channel_id={}, reason={}",
                        rule.getId(), binding.getChannelId(), message);
                failures.add(new PreparationFailure(binding.getChannelId(), message));
                continue;
            }
            try {
                AlarmNotificationChannelConfig channelConfig = channelConfigParser.parse(
                        channel.getMethodType(), channel.getConfig());
                AlarmDeliveryPayload payload = sender.prepare(rule, channel, channelConfig, metricResults);
                if (payload != null) {
                    deliveries.add(new PreparedDelivery(
                            channel.getId(), channel.getMethodType(), serializePayload(payload)));
                }
            } catch (RuntimeException failure) {
                String message = rootCauseMessage(failure);
                logger.error("Failed to prepare notification: rule_id={}, channel_id={}",
                        rule.getId(), binding.getChannelId(), failure);
                failures.add(new PreparationFailure(binding.getChannelId(), message));
            }
        }
        return new PreparationResult(deliveries, failures, null);
    }

    public void deliver(AlarmNotificationOutbox delivery) {
        AlarmSender sender = senderMap.get(delivery.getMethodType());
        if (sender == null) {
            throw new AlarmSendException("No sender for method_type: " + delivery.getMethodType());
        }
        AlarmDeliveryPayload payload = deserializePayload(delivery.getPayload());
        payload.validateFor(delivery.getMethodType());
        sender.send(delivery, payload);
    }

    private String serializePayload(AlarmDeliveryPayload payload) {
        try {
            return payloadWriter.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new AlarmSendException("Failed to serialize notification payload", e);
        }
    }

    private AlarmDeliveryPayload deserializePayload(String payload) {
        try {
            return payloadReader.readValue(payload);
        } catch (JsonProcessingException e) {
            throw new AlarmSendException("Failed to deserialize notification payload", e);
        }
    }

    public record PreparedDelivery(Long channelId,
                                   AlarmMethodType methodType,
                                   String payload) {
    }

    public record PreparationFailure(Long channelId, String message) {
        public PreparationFailure {
            Objects.requireNonNull(message, "message");
        }
    }

    public record PreparationResult(List<PreparedDelivery> deliveries,
                                    List<PreparationFailure> failures,
                                    String skippedReason) {
        public PreparationResult(List<PreparedDelivery> deliveries, String skippedReason) {
            this(deliveries, List.of(), skippedReason);
        }

        public PreparationResult {
            deliveries = List.copyOf(deliveries);
            failures = List.copyOf(failures);
        }

        public static PreparationResult failed(String message) {
            return new PreparationResult(
                    List.of(), List.of(new PreparationFailure(null, message)), null);
        }
    }
}
