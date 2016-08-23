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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v1;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component("cpuLoadCodecV1")
public class CpuLoadCodecV1 implements AgentStatCodec<CpuLoadBo> {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    private final HeaderCodecV1<Long> longHeaderCodec;

    @Autowired
    public CpuLoadCodecV1(AgentStatDataPointCodec codec, HeaderCodecV1<Long> longHeaderCodec) {
        Assert.notNull(codec, "agentStatDataPointCodec must not be null");
        Assert.notNull(longHeaderCodec, "longHeaderCodec must not be null");
        this.codec = codec;
        this.longHeaderCodec = longHeaderCodec;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<CpuLoadBo> cpuLoadBos) {
        if (CollectionUtils.isEmpty(cpuLoadBos)) {
            throw new IllegalArgumentException("cpuLoadBos must not be empty");
        }
        final int numValues = cpuLoadBos.size();
        valueBuffer.putVInt(numValues);

        List<Long> timestamps = new ArrayList<>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder jvmCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder systemCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        for (CpuLoadBo cpuLoadBo : cpuLoadBos) {
            timestamps.add(cpuLoadBo.getTimestamp());
            jvmCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(cpuLoadBo.getJvmCpuLoad()));
            systemCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(cpuLoadBo.getSystemCpuLoad()));
        }
        this.codec.encodeTimestamps(valueBuffer, timestamps);
        this.encodeDataPoints(valueBuffer, jvmCpuLoadAnalyzerBuilder.build(), systemCpuLoadAnalyzerBuilder.build());
    }

    private void encodeDataPoints(
            Buffer valueBuffer,
            StrategyAnalyzer<Long> jvmCpuLoadStrategyAnalyzer,
            StrategyAnalyzer<Long> systemCpuLoadStrategyAnalyzer) {
        // encode header
        int header = 0;
        int position = 0;
        header = this.longHeaderCodec.encodeHeader(header, position, jvmCpuLoadStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, systemCpuLoadStrategyAnalyzer.getBestStrategy());
        valueBuffer.putVInt(header);
        // encode values
        this.codec.encodeValues(valueBuffer, jvmCpuLoadStrategyAnalyzer.getBestStrategy(), jvmCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, systemCpuLoadStrategyAnalyzer.getBestStrategy(), systemCpuLoadStrategyAnalyzer.getValues());
    }

    @Override
    public List<CpuLoadBo> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        final String agentId = decodingContext.getAgentId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();

        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        int header = valueBuffer.readVInt();
        int position = 0;
        EncodingStrategy<Long> jvmCpuLoadEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> systemCpuLoadEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        // decode values
        List<Long> jvmCpuLoads = this.codec.decodeValues(valueBuffer, jvmCpuLoadEncodingStrategy, numValues);
        List<Long> systemCpuLoads = this.codec.decodeValues(valueBuffer, systemCpuLoadEncodingStrategy, numValues);

        List<CpuLoadBo> cpuLoadBos = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; ++i) {
            CpuLoadBo cpuLoadBo = new CpuLoadBo();
            cpuLoadBo.setAgentId(agentId);
            cpuLoadBo.setTimestamp(timestamps.get(i));
            cpuLoadBo.setJvmCpuLoad(AgentStatUtils.convertLongToDouble(jvmCpuLoads.get(i)));
            cpuLoadBo.setSystemCpuLoad(AgentStatUtils.convertLongToDouble(systemCpuLoads.get(i)));
            cpuLoadBos.add(cpuLoadBo);
        }
        return cpuLoadBos;
    }
}
