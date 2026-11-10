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

package com.navercorp.pinpoint.otlp.trace.inspector;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Outcome of mapping one Resource's metrics: the {@link AgentStatBo} to store ({@code null} when no
 * point produced a value) and the counts the collector reports as metrics. {@code mapped} counts the
 * stat points produced per {@link AgentStatType}; {@code dropped} counts input data points per reason.
 */
public final class MappingResult {

    private final @Nullable AgentStatBo agentStatBo;
    private final int receivedPoints;
    private final Map<AgentStatType, Integer> mapped;
    private final Map<InspectorDropReason, Integer> dropped;
    private final Map<String, Integer> acceptedByDialect;

    MappingResult(@Nullable AgentStatBo agentStatBo, int receivedPoints,
                  Map<AgentStatType, Integer> mapped, Map<InspectorDropReason, Integer> dropped,
                  Map<String, Integer> acceptedByDialect) {
        this.agentStatBo = agentStatBo;
        this.receivedPoints = receivedPoints;
        this.mapped = Collections.unmodifiableMap(mapped);
        this.dropped = Collections.unmodifiableMap(dropped);
        this.acceptedByDialect = Collections.unmodifiableMap(acceptedByDialect);
    }

    public @Nullable AgentStatBo getAgentStatBo() {
        return agentStatBo;
    }

    public boolean isEmpty() {
        return agentStatBo == null;
    }

    public int getReceivedPoints() {
        return receivedPoints;
    }

    public Map<AgentStatType, Integer> getMapped() {
        return mapped;
    }

    public Map<InspectorDropReason, Integer> getDropped() {
        return dropped;
    }

    public int getMapped(AgentStatType type) {
        return mapped.getOrDefault(type, 0);
    }

    public int getDropped(InspectorDropReason reason) {
        return dropped.getOrDefault(reason, 0);
    }

    /** Input points folded into a value, by the dialect that named them ({@code otel}, {@code micrometer}). */
    public Map<String, Integer> getAcceptedByDialect() {
        return acceptedByDialect;
    }

    public int getAccepted(String dialect) {
        return acceptedByDialect.getOrDefault(dialect, 0);
    }

    static final class Counters {
        private final Map<AgentStatType, Integer> mapped = new EnumMap<>(AgentStatType.class);
        private final Map<InspectorDropReason, Integer> dropped = new EnumMap<>(InspectorDropReason.class);
        private final Map<String, Integer> acceptedByDialect = new TreeMap<>();
        private int received;

        void received(int points) {
            received += points;
        }

        void accepted(String dialect) {
            acceptedByDialect.merge(dialect, 1, Integer::sum);
        }

        void mapped(AgentStatType type) {
            mapped.merge(type, 1, Integer::sum);
        }

        void dropped(InspectorDropReason reason, int points) {
            if (points > 0) {
                dropped.merge(reason, points, Integer::sum);
            }
        }

        MappingResult build(@Nullable AgentStatBo agentStatBo) {
            return new MappingResult(agentStatBo, received, mapped, dropped, acceptedByDialect);
        }
    }

    @Override
    public String toString() {
        return "MappingResult{" +
                "received=" + receivedPoints +
                ", mapped=" + mapped +
                ", dropped=" + dropped +
                ", accepted=" + acceptedByDialect +
                '}';
    }
}
