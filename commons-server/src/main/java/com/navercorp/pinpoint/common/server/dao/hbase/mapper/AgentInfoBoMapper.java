/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.JvmInfoBo;
import com.navercorp.pinpoint.common.server.bo.ServerMetaDataBo;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import static com.navercorp.pinpoint.common.hbase.HbaseColumnFamily.AGENTINFO_INFO;


/**
 * @author HyunGil Jeong
 */
@Component
public class AgentInfoBoMapper implements RowMapper<AgentInfoBo> {

    @Override
    public AgentInfoBo mapRow(Result result, int rowNum) throws Exception {
        byte[] rowKey = result.getRow();
        AgentId agentId = AgentId.of(BytesUtils.toStringAndRightTrim(rowKey, 0, PinpointConstants.AGENT_ID_MAX_LEN));
        long reverseStartTime = BytesUtils.bytesToLong(rowKey, HbaseTableConstants.AGENT_ID_MAX_LEN);
        long startTime = TimeUtils.recoveryTimeMillis(reverseStartTime);

        final byte[] serializedAgentInfo = result.getValue(AGENTINFO_INFO.getName(), AGENTINFO_INFO.QUALIFIER_IDENTIFIER);
        final AgentInfoBo.Builder agentInfoBoBuilder = createBuilderFromValue(serializedAgentInfo);
        agentInfoBoBuilder.setAgentId(agentId);
        agentInfoBoBuilder.setStartTime(startTime);

        final byte[] serializedServerMetaData = result.getValue(AGENTINFO_INFO.getName(), AGENTINFO_INFO.QUALIFIER_SERVER_META_DATA);
        if (serializedServerMetaData != null) {
            agentInfoBoBuilder.setServerMetaData(new ServerMetaDataBo.Builder(serializedServerMetaData).build());
        }

        final byte[] serializedJvmInfo = result.getValue(AGENTINFO_INFO.getName(), AGENTINFO_INFO.QUALIFIER_JVM);
        if (serializedJvmInfo != null) {
            agentInfoBoBuilder.setJvmInfo(new JvmInfoBo(serializedJvmInfo));
        }
        return agentInfoBoBuilder.build();
    }

    private AgentInfoBo.Builder createBuilderFromValue(byte[] serializedAgentInfo) {
        final Buffer buffer = new FixedBuffer(serializedAgentInfo);
        final AgentInfoBo.Builder builder = new AgentInfoBo.Builder();
        builder.setHostName(buffer.readPrefixedString());
        builder.setIp(buffer.readPrefixedString());
        builder.setPorts(buffer.readPrefixedString());
        builder.setApplicationName(buffer.readPrefixedString());
        builder.setServiceTypeCode(buffer.readShort());
        builder.setPid(buffer.readInt());
        builder.setAgentVersion(buffer.readPrefixedString());
        builder.setStartTime(buffer.readLong());
        builder.setEndTimeStamp(buffer.readLong());
        builder.setEndStatus(buffer.readInt());
        // FIXME - 2015.09 v1.5.0 added vmVersion (check for compatibility)
        if (buffer.hasRemaining()) {
            builder.setVmVersion(buffer.readPrefixedString());
        }
        // FIXME - 2018.06 v1.8.0 added container (check for compatibility)
        if (buffer.hasRemaining()) {
            builder.isContainer(buffer.readBoolean());
        }
        // 2021.03.24 added agent name
        if (buffer.hasRemaining()) {
            builder.setAgentName(buffer.readPrefixedString());
        }
        // 2024.03.11 added application id
        if (buffer.hasRemaining()) {
            builder.setApplicationId(ApplicationId.of(buffer.readUUID()));
            builder.setServiceName(buffer.readPrefixedString());
            builder.setServiceId(ServiceId.of(buffer.readUUID()));
        }
        return builder;
    }
}
