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

package com.navercorp.pinpoint.inspector.collector.model.kafka;

import com.navercorp.pinpoint.common.server.bo.stat.StatDataPoint;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class AgentStatList {
    private final List<AgentStat> list;

    public AgentStatList(int initialCapacity) {
        this.list = new ArrayList<>(initialCapacity);
    }

    public Collector newCollect(String tenantId, StatDataPoint dataPoint) {
        return new Collector(tenantId, dataPoint);
    }

    public List<AgentStat> build() {
        return list;
    }


    public class Collector {

        private final AgentStatBuilder builder;

        public Collector(String tenantId, StatDataPoint dataPoint) {
            this.builder = new AgentStatBuilder(tenantId, dataPoint);
        }

        public void collect(AgentStatField fieldName, double fieldValue) {
            AgentStat stat = builder.build(fieldName, fieldValue);
            list.add(stat);
        }

        public void collect(AgentStatField fieldName, double fieldValue, List<Tag> tags) {
            AgentStat stat = builder.build(fieldName, fieldValue, tags);
            list.add(stat);
        }
    }
}
