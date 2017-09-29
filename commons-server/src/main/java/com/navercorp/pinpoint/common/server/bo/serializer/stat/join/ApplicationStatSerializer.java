/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.bo.serializer.stat.join;

import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SerializationContext;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.util.Assert;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author minwoo.jung
 */
public abstract class ApplicationStatSerializer implements HbaseSerializer<List<JoinStatBo>, Put> {

    private final ApplicationStatEncoder encoder;

    protected ApplicationStatSerializer(ApplicationStatEncoder encoder) {
        Assert.notNull(encoder, "encoder must not be null");
        this.encoder = encoder;
    }

    @Override
    public void serialize(List<JoinStatBo> joinStatBoList, Put put, SerializationContext context) {
        if (CollectionUtils.isEmpty(joinStatBoList)) {
            throw new IllegalArgumentException("agentStatBos should not be empty");
        }
        long initialTimestamp = joinStatBoList.get(0).getTimestamp();
        long baseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        long timestampDelta = initialTimestamp - baseTimestamp;
        ByteBuffer qualifierBuffer = this.encoder.encodeQualifier(timestampDelta);
        ByteBuffer valueBuffer = this.encoder.encodeValue(joinStatBoList);
        put.addColumn(HBaseTables.APPLICATION_STAT_CF_STATISTICS, qualifierBuffer, HConstants.LATEST_TIMESTAMP, valueBuffer);
    }
}
