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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class AgentUriStatData implements MetricType {
    private final int capacity;
    private final long baseTimestamp;

    private final Map<URIKey, EachUriStatData> eachUriStatDataMap = new HashMap<>();

    public AgentUriStatData(long baseTimestamp, int capacity) {
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
        long endTime = uriStatInfo.getEndTime();
        return new URIKey(uri, endTime);
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
