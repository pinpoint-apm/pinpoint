/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.sampling.tail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Objects;

/**
 * Flushes traces whose root span never arrived: after bufferTtl elapses, forces a default-keep
 * decision so slow/incomplete traces are never lost. In a cluster, decide()'s SET NX ensures
 * only one node flushes each trace.
 */
public class TailSamplingSweeper {

    private final Logger logger = LogManager.getLogger(getClass());

    private static final int BATCH_LIMIT = 500;

    private final TailSamplingRepository repository;
    private final TailSampler tailSampler;
    private final TailSamplingProperties properties;

    public TailSamplingSweeper(TailSamplingRepository repository,
                               TailSampler tailSampler,
                               TailSamplingProperties properties) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.tailSampler = Objects.requireNonNull(tailSampler, "tailSampler");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Scheduled(fixedDelayString = "${collector.sampling.tail.sweep-interval:5s}")
    public void sweep() {
        try {
            long threshold = System.currentTimeMillis() - properties.getBufferTtl().toMillis();
            List<String> stale = repository.findStale(threshold, BATCH_LIMIT);
            for (String txid : stale) {
                List<byte[]> won = repository.decide(txid, true); // default keep
                if (won != null) {
                    tailSampler.replay(won);
                }
            }
            if (!stale.isEmpty() && logger.isInfoEnabled()) {
                logger.info("tail sampling sweeper flushed {} stale traces (default keep)", stale.size());
            }
        } catch (Exception e) {
            logger.warn("tail sampling sweeper error", e);
        }
    }
}
