/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.uri;

import com.navercorp.pinpoint.common.profiler.clock.TickClock;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class AgentUriStatData implements MetricType {
    private static final Logger LOGGER = LogManager.getLogger(AgentUriStatData.class);
    private final int capacity;
    private final long baseTimestamp;
    private final TickClock clock;
    private final Map<URIKey, EachUriStatData> eachUriStatDataMap = new HashMap<>();

    public AgentUriStatData(long baseTimestamp, int capacity, TickClock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
        Assert.isTrue(capacity > 0, "capacity must be  ` > 0`");
        this.capacity = capacity;
        Assert.isTrue(baseTimestamp > 0, "baseTimestamp must be  ` > 0`");
        this.baseTimestamp = baseTimestamp;
    }

    public int getCapacity() {
        return capacity;
    }

    public long getBaseTimestamp() {
        return baseTimestamp;
    }

    public boolean add(UriStatInfo uriStatInfo) {
        if (eachUriStatDataMap.size() >= this.capacity) {
            return false;
        }

        if (uriStatInfo.getEndTime() == 0L) {
            LOGGER.info("Cannot add collected uri stat info: endTime is 0 for {}", uriStatInfo.getUri());
            return true;
        }

        URIKey key = newURIKey(uriStatInfo);

        EachUriStatData eachUriStatData = eachUriStatDataMap.get(key);
        if (eachUriStatData == null) {
            eachUriStatData = new EachUriStatData(key.getUri());
            eachUriStatDataMap.put(key, eachUriStatData);
        }

        eachUriStatData.add(uriStatInfo);
        return true;
    }

    private URIKey newURIKey(UriStatInfo uriStatInfo) {
        String uri = uriStatInfo.getUri();
        long tickTime = clock.tick(uriStatInfo.getEndTime());
        return new URIKey(uri, tickTime);
    }

    public Set<Map.Entry<URIKey, EachUriStatData>> getAllUriStatData() {
        return eachUriStatDataMap.entrySet();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentUriStatData{");
        sb.append("baseTimestamp=").append(baseTimestamp);
        sb.append(", eachUriStatDataMap=").append(eachUriStatDataMap);
        sb.append('}');
        return sb.toString();
    }
}
