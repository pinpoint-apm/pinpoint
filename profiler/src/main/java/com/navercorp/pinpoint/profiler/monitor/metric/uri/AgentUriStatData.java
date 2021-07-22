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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class AgentUriStatData {

    private final long baseTimestamp;

    private Map<String, EachUriStatData> eachUriStatDataMap = new HashMap<>();

    public AgentUriStatData(long baseTimestamp) {
        Assert.isTrue(baseTimestamp > 0, "baseTimestamp must be  ` > 0`");

        this.baseTimestamp = baseTimestamp;
    }

    public long getBaseTimestamp() {
        return baseTimestamp;
    }

    public void add(UriStatInfo uriStatInfo) {
        String uri = uriStatInfo.getUri();

        EachUriStatData eachUriStatData = eachUriStatDataMap.get(uri);
        if (eachUriStatData == null) {
            eachUriStatData = new EachUriStatData(uri);
            eachUriStatDataMap.put(uri, eachUriStatData);
        }

        eachUriStatData.add(uriStatInfo);
    }

    public Collection<EachUriStatData> getAllUriStatData() {
        return eachUriStatDataMap.values();
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
