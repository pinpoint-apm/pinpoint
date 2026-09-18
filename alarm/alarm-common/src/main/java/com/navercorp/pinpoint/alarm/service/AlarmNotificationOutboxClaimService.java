/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.service;

import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Claims a bounded outbox batch in a short transaction.
 */
@Service
public class AlarmNotificationOutboxClaimService {

    private final AlarmNotificationOutboxDao outboxDao;
    private final TransactionTemplate requiresNew;
    private final Set<String> dataSources;

    /**
     * Claims only the notifications of rules this process evaluates -- the same data sources
     * the sweep reads. Processes share the outbox, and each one renders only its own data
     * sources' links and pages correctly.
     */
    @Autowired
    public AlarmNotificationOutboxClaimService(AlarmNotificationOutboxDao outboxDao,
                                               @Qualifier("transactionManager")
                                               PlatformTransactionManager transactionManager,
                                               List<MetricQueryService> metricQueryServices) {
        this(outboxDao, transactionManager, metricQueryServices.stream()
                .map(service -> service.getDataSource().name())
                .collect(Collectors.toUnmodifiableSet()));
    }

    public AlarmNotificationOutboxClaimService(AlarmNotificationOutboxDao outboxDao,
                                               PlatformTransactionManager transactionManager,
                                               Set<String> dataSources) {
        this.outboxDao = Objects.requireNonNull(outboxDao, "outboxDao");
        this.requiresNew = new TransactionTemplate(Objects.requireNonNull(transactionManager, "transactionManager"));
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.dataSources = Set.copyOf(Objects.requireNonNull(dataSources, "dataSources"));
        // An empty IN list is invalid SQL; the sweep refuses to start the same way.
        if (this.dataSources.isEmpty()) {
            throw new IllegalStateException(
                    "No metric query service is installed, so this process owns no notification to send");
        }
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
            List<Long> candidateIds = outboxDao.selectClaimCandidateIds(now, limit, dataSources);
            if (candidateIds.isEmpty()) {
                return List.<AlarmNotificationOutbox>of();
            }
            int count = outboxDao.claimAvailable(claimToken, now, now.plus(leaseDuration), candidateIds);
            return count == 0 ? List.<AlarmNotificationOutbox>of() : List.copyOf(outboxDao.selectByClaimToken(claimToken));
        });
        return claimed == null ? List.of() : claimed;
    }
}
