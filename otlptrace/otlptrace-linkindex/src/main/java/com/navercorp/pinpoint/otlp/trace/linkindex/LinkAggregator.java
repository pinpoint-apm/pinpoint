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

package com.navercorp.pinpoint.otlp.trace.linkindex;

import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Aggregates matched calls per {@link LinkKey}. Not thread-safe: use one per worker and {@link #merge}.
 */
public final class LinkAggregator {

    private final IntFunction<ServiceType> serviceTypeLookup;
    private final int weight;
    private final Map<LinkKey, LinkStat> stats = new HashMap<>();

    /**
     * @param serviceTypeLookup resolves a service type code
     * @param weight            count of one matched call: the trace sampling modulo, 1 when sampling is off
     */
    public LinkAggregator(IntFunction<ServiceType> serviceTypeLookup, int weight) {
        this.serviceTypeLookup = Objects.requireNonNull(serviceTypeLookup, "serviceTypeLookup");
        if (weight < 1) {
            throw new IllegalArgumentException("weight must be positive:" + weight);
        }
        this.weight = weight;
    }

    public void add(MatchedPair pair) {
        final LinkEntry server = pair.server();
        final LinkEntry client = pair.client();
        final Vertex callee = Vertex.of(server.serviceUid(), server.applicationName(), serviceTypeLookup.apply(server.serviceType()));
        final Vertex caller = Vertex.of(client.serviceUid(), client.applicationName(), serviceTypeLookup.apply(client.serviceType()));
        final LinkKey key = new LinkKey(callee, caller, client.destinationId(), pair.slot(), pair.source());
        final LinkStat stat = stats.computeIfAbsent(key, k -> new LinkStat(callee.serviceType().getHistogramSchema()));
        stat.add(server.elapsed(), server.error(), weight);
    }

    public void merge(LinkAggregator other) {
        other.stats.forEach((key, stat) -> {
            final LinkStat exist = stats.get(key);
            if (exist == null) {
                stats.put(key, stat);
            } else {
                exist.merge(stat);
            }
        });
    }

    public Map<LinkKey, LinkStat> getStats() {
        return Collections.unmodifiableMap(stats);
    }
}
