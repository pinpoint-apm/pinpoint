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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Component
public class CpuLoadCodec implements ApplicationStatCodec<JoinCpuLoadBo> {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    public CpuLoadCodec(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinCpuLoadBo> joinCpuLoadBoList) {
        if (CollectionUtils.isEmpty(joinCpuLoadBoList)) {
            throw new IllegalArgumentException("cpuLoadBoList must not be empty");
        }

        final int numValues = joinCpuLoadBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<>(numValues);
        JoinLongFieldStrategyAnalyzer.Builder jvmCpuLoadAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();
        JoinLongFieldStrategyAnalyzer.Builder systemCpuLoadAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();
        for (JoinCpuLoadBo joinCpuLoadBo : joinCpuLoadBoList) {
            timestamps.add(joinCpuLoadBo.getTimestamp());
            jvmCpuLoadAnalyzerBuilder.addValue(longFormat(joinCpuLoadBo.getJvmCpuLoadJoinValue()));
            systemCpuLoadAnalyzerBuilder.addValue(longFormat(joinCpuLoadBo.getSystemCpuLoadJoinValue()));
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
    public List<JoinCpuLoadBo> decodeValues(Buffer valueBuffer, ApplicationStatDecodingContext decodingContext) {
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

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; i++) {
            JoinCpuLoadBo joinCpuLoadBo = new JoinCpuLoadBo();
            joinCpuLoadBo.setId(id);
            joinCpuLoadBo.setTimestamp(timestamps.get(i));
            joinCpuLoadBo.setJvmCpuLoadJoinValue(doubleFormat(jvmCpuLoadList.get(i)));
            joinCpuLoadBo.setSystemCpuLoadJoinValue(doubleFormat(systemCpuLoadList.get(i)));
            joinCpuLoadBoList.add(joinCpuLoadBo);
        }
        return joinCpuLoadBoList;
    }

    public JoinDoubleFieldBo doubleFormat(JoinLongFieldBo field) {
        double avg = AgentStatUtils.convertLongToDouble(field.getAvg());
        double min = AgentStatUtils.convertLongToDouble(field.getMin());
        double max = AgentStatUtils.convertLongToDouble(field.getMax());
        return new JoinDoubleFieldBo(avg, min, field.getMinAgentId(), max, field.getMaxAgentId());
    }

    public JoinLongFieldBo longFormat(JoinDoubleFieldBo field) {
        final long avg = AgentStatUtils.convertDoubleToLong(field.getAvg());
        final long min = AgentStatUtils.convertDoubleToLong(field.getMin());
        final long max = AgentStatUtils.convertDoubleToLong(field.getMax());
        return new JoinLongFieldBo(avg, min, field.getMinAgentId(), max, field.getMaxAgentId());
    }
}
