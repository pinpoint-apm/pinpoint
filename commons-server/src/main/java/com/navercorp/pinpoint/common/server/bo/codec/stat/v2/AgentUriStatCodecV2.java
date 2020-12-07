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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Component("agentUriStatCodecV2")
public class AgentUriStatCodecV2 implements AgentStatCodec<AgentUriStatBo> {

    private static final byte VERSION = 2;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public AgentUriStatCodecV2(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<AgentUriStatBo> agentUriStatBoList) {
        final int numValues = CollectionUtils.nullSafeSize(agentUriStatBoList);
        if (numValues != 1) {
            return;
        }

        AgentUriStatBo agentUriStatBo = agentUriStatBoList.get(0);

        long startTimestamp = agentUriStatBo.getStartTimestamp();
        valueBuffer.putVLong(startTimestamp);

        long timestamp = agentUriStatBo.getTimestamp();
        valueBuffer.putVLong(timestamp);

        byte bucketVersion = agentUriStatBo.getBucketVersion();
        valueBuffer.putByte(bucketVersion);

        List<EachUriStatBo> eachUriStatBoList = agentUriStatBo.getEachUriStatBoList();
        int eachUriStatBoSize = CollectionUtils.nullSafeSize(eachUriStatBoList);
        valueBuffer.putVInt(eachUriStatBoSize);

        EachUriStatCodecV2 eachUriStatCodecV2 = new EachUriStatCodecV2(codec);
        eachUriStatCodecV2.encodeValues(valueBuffer, eachUriStatBoList);
    }

    @Override
    public List<AgentUriStatBo> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        AgentUriStatBo agentUriStatBo = new AgentUriStatBo();

        final String agentId = decodingContext.getAgentId();
        agentUriStatBo.setAgentId(agentId);

        final long startTimeStamp = valueBuffer.readVLong();
        agentUriStatBo.setStartTimestamp(startTimeStamp);

        final long timestamp = valueBuffer.readVLong();
        agentUriStatBo.setTimestamp(timestamp);

        final byte bucketVersion = valueBuffer.readByte();
        agentUriStatBo.setBucketVersion(bucketVersion);

        EachUriStatCodecV2 eachUriStatCodecV2 = new EachUriStatCodecV2(codec);
        List<EachUriStatBo> eachUriStatBoList = eachUriStatCodecV2.decodeValues(valueBuffer, decodingContext);
        agentUriStatBo.addAllEachUriStatBo(eachUriStatBoList);

        return Arrays.asList(agentUriStatBo);
    }

}
