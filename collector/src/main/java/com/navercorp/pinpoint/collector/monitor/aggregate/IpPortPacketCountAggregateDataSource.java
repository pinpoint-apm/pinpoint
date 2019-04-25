/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.monitor.aggregate;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HyunGil Jeong
 */
public class IpPortPacketCountAggregateDataSource implements AggregateDataSource {

    private static final String AGGREGATE_NAME = "Packet Count by Ip:Port";

    private final IpPortPacketCountAggregator aggregator;

    public IpPortPacketCountAggregateDataSource(IpPortPacketCountAggregator aggregator) {
        this.aggregator = Objects.requireNonNull(aggregator, "aggregator must not be null");
    }

    @Override
    public String getName() {
        return AGGREGATE_NAME;
    }

    @Override
    public List<Aggregate> getAggregates() {
        Map<IpPortPacketCountAggregator.IpPort, Long> countMap = aggregator.getAndReset();
        return countMap.entrySet().stream()
                .map(IpPortPacketCountAggregate::new)
                .sorted(IpPortPacketCountAggregate.KEY_ASCENDING_ORDER)
                .collect(Collectors.toList());
    }

    private static class IpPortPacketCountAggregate implements Aggregate<String, Long> {

        private static final Comparator<IpPortPacketCountAggregate> KEY_ASCENDING_ORDER = (o1, o2) -> o1.ipPort.compareToIgnoreCase(o2.ipPort);

        private final String ipPort;
        private final Long count;

        private IpPortPacketCountAggregate(Map.Entry<IpPortPacketCountAggregator.IpPort, Long> entry) {
            IpPortPacketCountAggregator.IpPort ipPort = entry.getKey();
            if (ipPort == null) {
                this.ipPort = "";
            } else {
                this.ipPort = ipPort.toString();
            }
            this.count = entry.getValue();
        }

        @Override
        public String getKey() {
            return ipPort;
        }

        @Override
        public Long getValue() {
            return count;
        }
    }
}
