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
package com.navercorp.pinpoint.collector.realtime.atc.service.redis;

import com.navercorp.pinpoint.collector.realtime.atc.dao.CountingMetricDao;
import com.navercorp.pinpoint.collector.realtime.atc.service.SupplyPublishService;
import com.navercorp.pinpoint.collector.realtime.atc.util.MinTermThrottle;
import com.navercorp.pinpoint.collector.realtime.atc.util.Throttle;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.realtime.atc.dto.ATCSupply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
public class RedisSupplyPublishService implements SupplyPublishService {

    private static final Logger logger = LogManager.getLogger(RedisSupplyPublishService.class);
    private static final String collectorIdForDebug = getCollectorIdForDebug();

    private final PubChannel<ATCSupply> supplyChannel;
    private final CountingMetricDao countingMetricDao;
    private final long minPublishTermNanos;

    private final Map<ClusterKey, Throttle> throttles = new ConcurrentHashMap<>();


    public RedisSupplyPublishService(
            PubChannel<ATCSupply> supplyChannel,
            CountingMetricDao countingMetricDao,
            long minPublishTermMillis
    ) {
        this.supplyChannel = Objects.requireNonNull(supplyChannel, "supplyChannel");
        this.countingMetricDao = Objects.requireNonNull(countingMetricDao, "countingMetricDao");
        this.minPublishTermNanos = TimeUnit.MILLISECONDS.toNanos(minPublishTermMillis);
    }

    @Override
    public void publish(ClusterKey clusterKey, List<Integer> values) {
        if (!getThrottle(clusterKey).hit()) {
            return;
        }

        final String applicationName = clusterKey.getApplicationName();
        final String agentId = clusterKey.getAgentId();
        final long startTimestamp = clusterKey.getStartTimestamp();

        final ATCSupply supply = makeSupply(agentId, startTimestamp, values);

        try {
            supplyChannel.publish(supply, applicationName);
            countingMetricDao.incrementCountATCSupply();
        } catch (Exception e) {
            logger.warn("Failed to publish", e);
        }
    }

    private Throttle getThrottle(ClusterKey clusterKey) {
        if (this.minPublishTermNanos <= 0) {
            return Throttle.alwaysTrue;
        }

        return this.throttles.computeIfAbsent(clusterKey, key -> new MinTermThrottle(this.minPublishTermNanos));
    }

    private ATCSupply makeSupply(String agentId, long startTimestamp, List<Integer> values) {
        final ATCSupply supply = new ATCSupply();
        supply.setAgentId(agentId);
        supply.setStartTimestamp(startTimestamp);
        supply.setCollectorId(collectorIdForDebug);
        supply.setValues(values);
        return supply;
    }

    private static String getCollectorIdForDebug() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return "unknown";
        }
    }

}
