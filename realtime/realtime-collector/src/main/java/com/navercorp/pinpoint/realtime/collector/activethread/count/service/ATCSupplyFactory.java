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
package com.navercorp.pinpoint.realtime.collector.activethread.count.service;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.realtime.util.MinTermThrottle;
import com.navercorp.pinpoint.realtime.util.Throttle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
class ATCSupplyFactory {

    private static final Logger logger = LogManager.getLogger(ATCSupplyFactory.class);
    private static final String collectorIdForDebug = getCollectorIdForDebug();

    private final long minPublishTermNanos;

    private final Map<ClusterKey, Throttle> throttles = new ConcurrentHashMap<>();

    ATCSupplyFactory(long minPublishTermMillis) {
        this.minPublishTermNanos = TimeUnit.MILLISECONDS.toNanos(minPublishTermMillis);
    }

    ATCSupply build(ClusterKey clusterKey, List<Integer> values) {
        if (values == null || !getThrottle(clusterKey).hit()) {
            return null;
        }

        try {
            return makeSupply(clusterKey, values, ATCSupply.Message.OK);
        } catch (Exception e) {
            logger.warn("Failed to publish", e);
            return null;
        }
    }

    ATCSupply buildConnectionNotifier(ClusterKey clusterKey) {
        return makeSupply(clusterKey, List.of(), ATCSupply.Message.CONNECTED);
    }

    private Throttle getThrottle(ClusterKey clusterKey) {
        if (this.minPublishTermNanos <= 0) {
            return Throttle.alwaysTrue;
        }

        return this.throttles.computeIfAbsent(clusterKey, key -> new MinTermThrottle(this.minPublishTermNanos));
    }

    private ATCSupply makeSupply(ClusterKey clusterKey, List<Integer> values, ATCSupply.Message message) {
        final ATCSupply supply = new ATCSupply();
        supply.setApplicationName(clusterKey.getApplicationName());
        supply.setAgentId(clusterKey.getAgentId());
        supply.setStartTimestamp(clusterKey.getStartTimestamp());
        supply.setCollectorId(collectorIdForDebug);
        supply.setValues(values);
        supply.setMessage(message);
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
