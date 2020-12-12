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

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentUriStatEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SerializationContext;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Component
public class AgentUriStatSerializer implements HbaseSerializer<List<AgentUriStatBo>, Put> {

    private final AgentUriStatEncoder agentUriStatEncoder;

    @Autowired
    public AgentUriStatSerializer(AgentUriStatEncoder agentUriStatEncoder) {
        this.agentUriStatEncoder = Objects.requireNonNull(agentUriStatEncoder, "agentUriStatEncoder");
    }

    @Override
    public void serialize(List<AgentUriStatBo> agentStatBos, Put put, SerializationContext context) {
        if (CollectionUtils.isEmpty(agentStatBos)) {
            throw new IllegalArgumentException("agentStatBos should not be empty");
        }
        long initialTimestamp = agentStatBos.get(0).getTimestamp();
        long baseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        long timestampDelta = initialTimestamp - baseTimestamp;
        ByteBuffer qualifierBuffer = this.agentUriStatEncoder.encodeQualifier(timestampDelta);
        ByteBuffer valueBuffer = this.agentUriStatEncoder.encodeValue(agentStatBos);

        put.addColumn(HbaseColumnFamily.AGENT_URI_STAT_STATISTICS.getName(), qualifierBuffer, HConstants.LATEST_TIMESTAMP, valueBuffer);
    }

}
