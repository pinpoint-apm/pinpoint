package com.navercorp.pinpoint.alarm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationService;
import com.navercorp.pinpoint.alarm.sender.AlarmSendException;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Sends claimed notification snapshots without holding a database transaction.
 */
@Service
public class AlarmNotificationDispatcher {

    /**
     * alarm_notification_outbox.attempt_count is TINYINT UNSIGNED, so 255 is the highest value
     * it can hold. A claim beyond the budget still increments once before the row is marked
     * dead, so the budget itself has to stop one short of the column.
     */
    private static final int MAX_ATTEMPT_COUNT = 254;

    private static final Logger logger = LogManager.getLogger(AlarmNotificationDispatcher.class);
    private static final List<Duration> DEFAULT_BACKOFF = List.of(
            Duration.ofMinutes(1),
            Duration.ofMinutes(5),
            Duration.ofMinutes(15),
            Duration.ofHours(1));

    private final AlarmNotificationOutboxClaimService claimService;
    private final AlarmNotificationService notificationService;
    private final AlarmNotificationResultService resultService;
    private final int batchSize;
    private final int maxAttempts;
    private final Duration leaseDuration;
    private final Clock clock;

    @Autowired
    public AlarmNotificationDispatcher(
            AlarmNotificationOutboxClaimService claimService,
            AlarmNotificationService notificationService,
            AlarmNotificationResultService resultService,
            @Value("${pinpoint.modules.batch.alarm.outbox.batchSize:100}") int batchSize,
            @Value("${pinpoint.modules.batch.alarm.outbox.maxAttempts:5}") int maxAttempts,
            @Value("${pinpoint.modules.batch.alarm.outbox.leaseSec:60}") long leaseSec) {
        this(claimService, notificationService, resultService, batchSize, maxAttempts,
                Duration.ofSeconds(leaseSec));
    }

    public AlarmNotificationDispatcher(AlarmNotificationOutboxClaimService claimService,
                                       AlarmNotificationService notificationService,
                                       AlarmNotificationResultService resultService,
                                       int batchSize,
                                       int maxAttempts,
                                       Duration leaseDuration) {
        this(claimService, notificationService, resultService, batchSize, maxAttempts,
                leaseDuration, Clock.systemUTC());
    }

    AlarmNotificationDispatcher(AlarmNotificationOutboxClaimService claimService,
                                AlarmNotificationService notificationService,
                                AlarmNotificationResultService resultService,
                                int batchSize,
                                int maxAttempts,
                                Duration leaseDuration,
                                Clock clock) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService");
        this.resultService = Objects.requireNonNull(resultService, "resultService");
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than zero");
        }
        if (maxAttempts <= 0 || maxAttempts > MAX_ATTEMPT_COUNT) {
            throw new IllegalArgumentException(
                    "maxAttempts must be between 1 and " + MAX_ATTEMPT_COUNT);
        }
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.leaseDuration = Objects.requireNonNull(leaseDuration, "leaseDuration");
        if (leaseDuration.isZero() || leaseDuration.isNegative()) {
            throw new IllegalArgumentException("leaseDuration must be greater than zero");
        }
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public int dispatch() {
        LocalDateTime claimTime = LocalDateTime.now(clock);
        List<AlarmNotificationOutbox> deliveries = claimService.claim(batchSize, claimTime, leaseDuration);
        for (AlarmNotificationOutbox delivery : deliveries) {
            dispatch(delivery);
        }
        return deliveries.size();
    }

    private void dispatch(AlarmNotificationOutbox delivery) {
        // Claiming increments attempt_count, and lease recovery claims a row whose worker died
        // before it could record a result -- so a row can come back with its budget already
        // spent. Without this it would be sent again on every recovery and the counter would
        // climb until the increment overflowed TINYINT UNSIGNED, and because a claim updates
        // the whole batch in one statement, that one row would fail every later claim.
        if (delivery.getAttemptCount() > maxAttempts) {
            if (resultService.markDead(delivery)) {
                logger.error("Alarm notification delivery is dead after lease recovery exhausted "
                                + "its attempts: delivery_id={}, attempt_count={}",
                        delivery.getId(), delivery.getAttemptCount());
            }
            return;
        }
        if (delivery.getAvailableAt() != null && !LocalDateTime.now(clock).isBefore(delivery.getAvailableAt())) {
            logger.warn("Skipping delivery because lease expired before send: delivery_id={}", delivery.getId());
            return;
        }
        try {
            notificationService.deliver(delivery);
        } catch (RuntimeException sendFailure) {
            try {
                recordFailure(delivery, sendFailure);
            } catch (RuntimeException resultFailure) {
                logger.error("Failed to record alarm notification failure; lease recovery will retry it: "
                                + "delivery_id={}", delivery.getId(), resultFailure);
            }
            return;
        }

        try {
            if (!resultService.markSent(delivery, LocalDateTime.now(clock))) {
                logger.warn("Delivery result was not recorded because its claim expired: delivery_id={}",
                        delivery.getId());
            }
        } catch (RuntimeException resultFailure) {
            logger.error("Failed to record successful alarm notification; lease recovery may send it again: "
                            + "delivery_id={}", delivery.getId(), resultFailure);
        }
    }

    private void recordFailure(AlarmNotificationOutbox delivery, RuntimeException failure) {
        boolean exhausted = delivery.getAttemptCount() >= maxAttempts;
        if (exhausted || isPermanent(failure)) {
            if (resultService.markDead(delivery)) {
                logger.error("Alarm notification delivery is dead: delivery_id={}, attempt_count={}",
                        delivery.getId(), delivery.getAttemptCount(), failure);
            } else {
                logger.warn("Dead result was ignored because the delivery claim expired: delivery_id={}",
                        delivery.getId());
            }
            return;
        }

        Duration delay = DEFAULT_BACKOFF.get(Math.min(
                Math.max(delivery.getAttemptCount() - 1, 0), DEFAULT_BACKOFF.size() - 1));
        LocalDateTime availableAt = LocalDateTime.now(clock).plus(delay);
        if (resultService.scheduleRetry(delivery, availableAt)) {
            logger.warn("Alarm notification delivery scheduled for retry: "
                            + "delivery_id={}, attempt_count={}, next={}",
                    delivery.getId(), delivery.getAttemptCount(), availableAt, failure);
        } else {
            logger.warn("Retry result was ignored because the delivery claim expired: delivery_id={}",
                    delivery.getId());
        }
    }

    private boolean isPermanent(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current instanceof HttpClientErrorException clientError) {
                int status = clientError.getStatusCode().value();
                return status != 408 && status != 429;
            }
            if (current instanceof JsonProcessingException || current instanceof IllegalArgumentException) {
                return true;
            }
        }
        return failure instanceof AlarmSendException && failure.getCause() == null;
    }

}
