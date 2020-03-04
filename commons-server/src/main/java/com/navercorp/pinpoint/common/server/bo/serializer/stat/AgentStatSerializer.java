/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SerializationContext;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Put;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public abstract class AgentStatSerializer<T extends AgentStatDataPoint> implements HbaseSerializer<List<T>, Put> {

    private final AgentStatEncoder<T> encoder;

    protected AgentStatSerializer(AgentStatEncoder<T> encoder) {
        this.encoder = Objects.requireNonNull(encoder, "encoder");
    }

    @Override
    public void serialize(List<T> agentStatBos, Put put, SerializationContext context) {
        if (CollectionUtils.isEmpty(agentStatBos)) {
            throw new IllegalArgumentException("agentStatBos should not be empty");
        }
        long initialTimestamp = agentStatBos.get(0).getTimestamp();
        long baseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        long timestampDelta = initialTimestamp - baseTimestamp;
        ByteBuffer qualifierBuffer = this.encoder.encodeQualifier(timestampDelta);
        ByteBuffer valueBuffer = this.encoder.encodeValue(agentStatBos);
        put.addColumn(HbaseColumnFamily.AGENT_STAT_STATISTICS.getName(), qualifierBuffer, HConstants.LATEST_TIMESTAMP, valueBuffer);
    }
}
