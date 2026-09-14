package com.navercorp.pinpoint.alarm.service;

import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Claims a bounded outbox batch in a short transaction.
 */
@Service
public class AlarmNotificationOutboxClaimService {

    private final AlarmNotificationOutboxDao outboxDao;
    private final TransactionTemplate requiresNew;

    public AlarmNotificationOutboxClaimService(AlarmNotificationOutboxDao outboxDao,
                                               @Qualifier("transactionManager")
                                               PlatformTransactionManager transactionManager) {
        this.outboxDao = Objects.requireNonNull(outboxDao, "outboxDao");
        this.requiresNew = new TransactionTemplate(Objects.requireNonNull(transactionManager, "transactionManager"));
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public List<AlarmNotificationOutbox> claim(int limit, LocalDateTime now, Duration leaseDuration) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than zero");
        }
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(leaseDuration, "leaseDuration");
        if (leaseDuration.isZero() || leaseDuration.isNegative()) {
            throw new IllegalArgumentException("leaseDuration must be greater than zero");
        }

        String claimToken = UUID.randomUUID().toString().replace("-", "");
        List<AlarmNotificationOutbox> claimed = requiresNew.execute(status -> {
            List<Long> candidateIds = outboxDao.selectClaimCandidateIds(now, limit);
            if (candidateIds.isEmpty()) {
                return List.<AlarmNotificationOutbox>of();
            }
            int count = outboxDao.claimAvailable(claimToken, now, now.plus(leaseDuration), candidateIds);
            return count == 0 ? List.<AlarmNotificationOutbox>of() : List.copyOf(outboxDao.selectByClaimToken(claimToken));
        });
        return claimed == null ? List.of() : claimed;
    }
}
