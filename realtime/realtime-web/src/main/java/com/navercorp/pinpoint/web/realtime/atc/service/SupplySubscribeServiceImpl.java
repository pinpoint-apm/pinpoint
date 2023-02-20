/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.realtime.atc.service;

import com.navercorp.pinpoint.pubsub.SubChannel;
import com.navercorp.pinpoint.pubsub.SubConsumer;
import com.navercorp.pinpoint.realtime.atc.dto.ATCSupply;
import com.navercorp.pinpoint.web.realtime.atc.dao.ATCValueDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.memory.ATCSessionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
public class SupplySubscribeServiceImpl implements SupplySubscribeService {

    private static final Logger logger = LogManager.getLogger(SupplySubscribeServiceImpl.class);

    private final Object subscriptionLock = new Object();

    private final ATCSessionRepository sessionRepository;
    private final SubChannel<ATCSupply> supplyChannel;
    private final SubConsumer<ATCSupply> consumer;

    public SupplySubscribeServiceImpl(
            ATCSessionRepository sessionRepository,
            SubChannel<ATCSupply> supplyChannel,
            ATCValueDao valueDao
    ) {
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository");
        this.supplyChannel = Objects.requireNonNull(supplyChannel, "supplyChannel");

        Objects.requireNonNull(valueDao, "valueDao");
        this.consumer = new SupplyConsumer(valueDao);
    }

    @Override
    public Set<String> updateSubscriptions(Set<String> prevApplications) {
        synchronized (subscriptionLock) {
            final Set<String> nextApplications = this.sessionRepository.getAllDemandedApplicationNames();
            subscribeNewTopics(prevApplications, nextApplications);
            unsubscribeOldTopics(prevApplications, nextApplications);
            return nextApplications;
        }
    }

    private void subscribeNewTopics(Set<String> prevApps, Set<String> nextApps) {
        for (final String next: nextApps) {
            if (!prevApps.contains(next)) {
                logger.info("Subscribe {}", next);
                this.supplyChannel.subscribe(this.consumer, next);
            }
        }
    }

    private void unsubscribeOldTopics(Set<String> prevTopics, Set<String> nextTopics) {
        for (final String prev: prevTopics) {
            if (!nextTopics.contains(prev)) {
                logger.info("Unsubscribe {}", prev);
                this.supplyChannel.unsubscribe(this.consumer, prev);
            }
        }
    }

    private static class SupplyConsumer implements SubConsumer<ATCSupply> {

        private final ATCValueDao valueRepository;

        SupplyConsumer(ATCValueDao valueRepository) {
            this.valueRepository = Objects.requireNonNull(valueRepository, "valueRepository");
        }

        @Override
        public void consume(ATCSupply content, String postfix) {
            valueRepository.put(postfix, content);
        }

    }

}
