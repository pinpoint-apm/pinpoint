package com.navercorp.pinpoint.alarm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.vo.AlarmHistoryV2;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutboxCounts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Records delivery outcomes and keeps the parent history summary in sync.
 */
@Service
public class AlarmNotificationResultService {

    private static final Logger logger = LogManager.getLogger(AlarmNotificationResultService.class);

    private final AlarmNotificationOutboxDao outboxDao;
    private final AlarmStateDao stateDao;
    private final AlarmHistoryV2Dao historyDao;
    private final TransactionTemplate requiresNew;
    private final ObjectReader contextReader;
    private final ObjectWriter contextWriter;

    public AlarmNotificationResultService(AlarmNotificationOutboxDao outboxDao,
                                          AlarmStateDao stateDao,
                                          AlarmHistoryV2Dao historyDao,
                                          @Qualifier("transactionManager")
                                          PlatformTransactionManager transactionManager,
                                          ObjectMapper objectMapper) {
        this.outboxDao = Objects.requireNonNull(outboxDao, "outboxDao");
        this.stateDao = Objects.requireNonNull(stateDao, "stateDao");
        this.historyDao = Objects.requireNonNull(historyDao, "historyDao");
        this.requiresNew = new TransactionTemplate(Objects.requireNonNull(transactionManager, "transactionManager"));
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.contextReader = objectMapper.readerFor(JsonNode.class);
        this.contextWriter = objectMapper.writerFor(JsonNode.class);
    }

    public boolean markSent(AlarmNotificationOutbox delivery, LocalDateTime sentAt) {
        Objects.requireNonNull(sentAt, "sentAt");
        return execute(delivery,
                () -> outboxDao.markSent(delivery.getId(), delivery.getClaimToken()),
                history -> {
                    stateDao.updateLastNotifiedAt(history.getRuleId(), sentAt);
                    updateHistorySummary(history);
                });
    }

    public boolean scheduleRetry(AlarmNotificationOutbox delivery,
                                 LocalDateTime availableAt) {
        Objects.requireNonNull(availableAt, "availableAt");
        return execute(delivery, () -> outboxDao.markRetry(
                delivery.getId(), delivery.getClaimToken(), availableAt));
    }

    public boolean markDead(AlarmNotificationOutbox delivery) {
        return execute(delivery,
                () -> outboxDao.markDead(delivery.getId(), delivery.getClaimToken()),
                this::updateHistorySummary);
    }

    private boolean execute(AlarmNotificationOutbox delivery,
                            StatusUpdate statusUpdate) {
        return execute(delivery, statusUpdate, null);
    }

    private boolean execute(AlarmNotificationOutbox delivery,
                            StatusUpdate statusUpdate,
                            HistoryUpdate historyUpdate) {
        Objects.requireNonNull(delivery, "delivery");
        Boolean updated = requiresNew.execute(status -> {
            if (statusUpdate.update() != 1) {
                return false;
            }
            if (historyUpdate == null) {
                return true;
            }
            AlarmHistoryV2 history = historyDao.selectByIdForUpdate(delivery.getHistoryId());
            if (history == null) {
                logger.warn("Alarm history not found while recording notification result: history_id={}",
                        delivery.getHistoryId());
                return true;
            }
            historyUpdate.update(history);
            return true;
        });
        return Boolean.TRUE.equals(updated);
    }

    private void updateHistorySummary(AlarmHistoryV2 history) {
        Long historyId = history.getId();
        AlarmNotificationOutboxCounts counts = outboxDao.selectCountsByHistoryId(historyId);

        try {
            JsonNode parsed = history.getContext() == null
                    ? JsonNodeFactory.instance.objectNode()
                    : contextReader.readValue(history.getContext());
            ObjectNode context = parsed instanceof ObjectNode
                    ? (ObjectNode) parsed
                    : JsonNodeFactory.instance.objectNode();
            JsonNode notificationNode = context.get("notification");
            ObjectNode notification = notificationNode instanceof ObjectNode
                    ? (ObjectNode) notificationNode
                    : context.putObject("notification");
            notification.put("sent", counts.sent());
            notification.put("failed", counts.dead());
            notification.put("pending", counts.pending());
            historyDao.updateContext(historyId, contextWriter.writeValueAsString(context));
        } catch (JsonProcessingException e) {
            logger.warn("Failed to update alarm history notification context: history_id={}", historyId, e);
        }
    }

    @FunctionalInterface
    private interface StatusUpdate {
        int update();
    }

    @FunctionalInterface
    private interface HistoryUpdate {
        void update(AlarmHistoryV2 history);
    }
}
