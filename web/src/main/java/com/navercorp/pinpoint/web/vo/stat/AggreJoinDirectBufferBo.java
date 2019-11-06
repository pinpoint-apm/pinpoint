/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;

/**
 * @author Roy Kim
 */
public class AggreJoinDirectBufferBo extends JoinDirectBufferBo implements AggregationStatData {

    public AggreJoinDirectBufferBo() {
    }

    public AggreJoinDirectBufferBo(String id, long avgDirectCount, long maxDirectCount, String maxDirectCountAgentId, long minDirectCount, String minDirectCountAgentId, long avgDirectMemoryUsed, long maxDirectMemoryUsed, String maxDirectMemoryUsedAgentId, long minDirectMemoryUsed, String minDirectMemoryUsedAgentId, long avgMappedCount, long maxMappedCount, String maxMappedCountAgentId, long minMappedCount, String minMappedCountAgentId, long avgMappedMemoryUsed, long maxMappedMemoryUsed, String maxMappedMemoryUsedAgentId, long minMappedMemoryUsed, String minMappedMemoryUsedAgentId, long timestamp) {
        super(id, avgDirectCount, maxDirectCount, maxDirectCountAgentId, minDirectCount, minDirectCountAgentId, avgDirectMemoryUsed, maxDirectMemoryUsed, maxDirectMemoryUsedAgentId, minDirectMemoryUsed, minDirectMemoryUsedAgentId, avgMappedCount, maxMappedCount, maxMappedCountAgentId, minMappedCount, minMappedCountAgentId, avgMappedMemoryUsed, maxMappedMemoryUsed, maxMappedMemoryUsedAgentId, minMappedMemoryUsed, minMappedMemoryUsedAgentId, timestamp);
    }

    public static AggreJoinDirectBufferBo createUncollectedObject(long timestamp) {
        AggreJoinDirectBufferBo aggreJoinDirectBufferBo = new AggreJoinDirectBufferBo();
        aggreJoinDirectBufferBo.setTimestamp(timestamp);
        return aggreJoinDirectBufferBo;
    }
}
