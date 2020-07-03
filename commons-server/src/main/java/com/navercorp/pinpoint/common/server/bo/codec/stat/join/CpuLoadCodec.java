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
package com.navercorp.pinpoint.common.server.bo.codec.stat.join;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.JoinLongFieldEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.JoinLongFieldStrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Component("joincpuLoadCodec")
public class CpuLoadCodec implements ApplicationStatCodec {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public CpuLoadCodec(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "agentStatDataPointCodec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinCpuLoadBoList) {
        if (CollectionUtils.isEmpty(joinCpuLoadBoList)) {
            throw new IllegalArgumentException("cpuLoadBoList must not be empty");
        }

        final int numValues = joinCpuLoadBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        JoinLongFieldStrategyAnalyzer.Builder jvmCpuLoadAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();
        JoinLongFieldStrategyAnalyzer.Builder systemCpuLoadAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();
        for (JoinStatBo joinStatBo : joinCpuLoadBoList) {
            JoinCpuLoadBo joinCpuLoadBo = (JoinCpuLoadBo) joinStatBo;
            timestamps.add(joinCpuLoadBo.getTimestamp());
            jvmCpuLoadAnalyzerBuilder.addValue(joinCpuLoadBo.getJvmCpuLoadJoinValue().toLongFieldBo());
            systemCpuLoadAnalyzerBuilder.addValue(joinCpuLoadBo.getSystemCpuLoadJoinValue().toLongFieldBo());
        }
        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, jvmCpuLoadAnalyzerBuilder.build(), systemCpuLoadAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer, JoinLongFieldStrategyAnalyzer jvmCpuLoadAnalyzer, JoinLongFieldStrategyAnalyzer systemCpuLoadAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        byte[] codes = jvmCpuLoadAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }
        codes = systemCpuLoadAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, jvmCpuLoadAnalyzer.getBestStrategy(), jvmCpuLoadAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, systemCpuLoadAnalyzer.getBestStrategy(), systemCpuLoadAnalyzer.getValues());
    }

    @Override
    public List<JoinStatBo> decodeValues(Buffer valueBuffer, ApplicationStatDecodingContext decodingContext) {
        final String id = decodingContext.getApplicationId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        JoinLongFieldEncodingStrategy jvmCpuLoadEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());
        JoinLongFieldEncodingStrategy systemCpuLoadEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());

        // decode values
        final List<JoinLongFieldBo> jvmCpuLoadList = this.codec.decodeValues(valueBuffer, jvmCpuLoadEncodingStrategy, numValues);
        final List<JoinLongFieldBo> systemCpuLoadList = this.codec.decodeValues(valueBuffer, systemCpuLoadEncodingStrategy, numValues);

        List<JoinStatBo> joinCpuLoadBoList = new ArrayList<JoinStatBo>(numValues);
        for (int i = 0; i < numValues; i++) {
            JoinCpuLoadBo joinCpuLoadBo = new JoinCpuLoadBo();
            joinCpuLoadBo.setId(id);
            joinCpuLoadBo.setTimestamp(timestamps.get(i));
            joinCpuLoadBo.setJvmCpuLoadJoinValue(jvmCpuLoadList.get(i).toLongFieldBo());
            joinCpuLoadBo.setSystemCpuLoadJoinValue(systemCpuLoadList.get(i).toLongFieldBo());
            joinCpuLoadBoList.add(joinCpuLoadBo);
        }
        return joinCpuLoadBoList;
    }
}
