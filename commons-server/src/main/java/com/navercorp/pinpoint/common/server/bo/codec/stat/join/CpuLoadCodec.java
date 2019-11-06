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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StringEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
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
        UnsignedLongEncodingStrategy.Analyzer.Builder jvmCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minJvmCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minJvmCpuAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxJvmCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxJvmCpuAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder systemCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minSystemCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minSysCpuAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxSystemCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxSysCpuAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        for (JoinStatBo joinStatBo : joinCpuLoadBoList) {
            JoinCpuLoadBo joinCpuLoadBo = (JoinCpuLoadBo) joinStatBo;
            timestamps.add(joinCpuLoadBo.getTimestamp());
            jvmCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(joinCpuLoadBo.getJvmCpuLoad()));
            minJvmCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(joinCpuLoadBo.getMinJvmCpuLoad()));
            minJvmCpuAgentIdAnalyzerBuilder.addValue(joinCpuLoadBo.getMinJvmCpuAgentId());
            maxJvmCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(joinCpuLoadBo.getMaxJvmCpuLoad()));
            maxJvmCpuAgentIdAnalyzerBuilder.addValue(joinCpuLoadBo.getMaxJvmCpuAgentId());
            systemCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(joinCpuLoadBo.getSystemCpuLoad()));
            minSystemCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(joinCpuLoadBo.getMinSystemCpuLoad()));
            minSysCpuAgentIdAnalyzerBuilder.addValue(joinCpuLoadBo.getMinSysCpuAgentId());
            maxSystemCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(joinCpuLoadBo.getMaxSystemCpuLoad()));
            maxSysCpuAgentIdAnalyzerBuilder.addValue(joinCpuLoadBo.getMaxSysCpuAgentId());
        }
        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, jvmCpuLoadAnalyzerBuilder.build(), minJvmCpuLoadAnalyzerBuilder.build(), minJvmCpuAgentIdAnalyzerBuilder.build(), maxJvmCpuLoadAnalyzerBuilder.build(), maxJvmCpuAgentIdAnalyzerBuilder.build(), systemCpuLoadAnalyzerBuilder.build(), minSystemCpuLoadAnalyzerBuilder.build(), minSysCpuAgentIdAnalyzerBuilder.build(), maxSystemCpuLoadAnalyzerBuilder.build(), maxSysCpuAgentIdAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer,
                    StrategyAnalyzer<Long> jvmCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<Long> minJvmCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<String> minJvmCpuAgentIdStrategyAnalyzer,
                    StrategyAnalyzer<Long> maxJvmCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<String> maxJvmCpuAgentIdStrategyAnalyzer,
                    StrategyAnalyzer<Long> systemCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<Long> minSystemCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<String> minSysCpuAgentIdStrategyAnalyzer,
                    StrategyAnalyzer<Long> maxSystemCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<String> maxSysCpuAgentIdStrategyAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(jvmCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minJvmCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minJvmCpuAgentIdStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxJvmCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxJvmCpuAgentIdStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(systemCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minSystemCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minSysCpuAgentIdStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxSystemCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxSysCpuAgentIdStrategyAnalyzer.getBestStrategy().getCode());
        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, jvmCpuLoadStrategyAnalyzer.getBestStrategy(), jvmCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minJvmCpuLoadStrategyAnalyzer.getBestStrategy(), minJvmCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minJvmCpuAgentIdStrategyAnalyzer.getBestStrategy(), minJvmCpuAgentIdStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxJvmCpuLoadStrategyAnalyzer.getBestStrategy(), maxJvmCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxJvmCpuAgentIdStrategyAnalyzer.getBestStrategy(), maxJvmCpuAgentIdStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, systemCpuLoadStrategyAnalyzer.getBestStrategy(), systemCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minSystemCpuLoadStrategyAnalyzer.getBestStrategy(), minSystemCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minSysCpuAgentIdStrategyAnalyzer.getBestStrategy(), minSysCpuAgentIdStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxSystemCpuLoadStrategyAnalyzer.getBestStrategy(), maxSystemCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxSysCpuAgentIdStrategyAnalyzer.getBestStrategy(), maxSysCpuAgentIdStrategyAnalyzer.getValues());
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
        EncodingStrategy<Long> jvmCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> minJvmCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minJvmCpuAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> maxJvmCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxJvmCpuAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> systemCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> minSystemCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minSysCpuAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> maxSystemCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxSysCpuAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        // decode values
        List<Long> jvmCpuLoads = this.codec.decodeValues(valueBuffer, jvmCpuLoadEncodingStrategy, numValues);
        List<Long> minJvmCpuLoads = this.codec.decodeValues(valueBuffer, minJvmCpuLoadEncodingStrategy, numValues);
        List<String> minJvmCpuAgentIds = this.codec.decodeValues(valueBuffer, minJvmCpuAgentIdEncodingStrategy, numValues);
        List<Long> maxJvmCpuLoads = this.codec.decodeValues(valueBuffer, maxJvmCpuLoadEncodingStrategy, numValues);
        List<String> maxJvmCpuAgentIds = this.codec.decodeValues(valueBuffer, maxJvmCpuAgentIdEncodingStrategy, numValues);
        List<Long> systemCpuLoads = this.codec.decodeValues(valueBuffer, systemCpuLoadEncodingStrategy, numValues);
        List<Long> minSystemCpuLoads = this.codec.decodeValues(valueBuffer, minSystemCpuLoadEncodingStrategy, numValues);
        List<String> minSysCpuAgentIds = this.codec.decodeValues(valueBuffer, minSysCpuAgentIdEncodingStrategy, numValues);
        List<Long> maxSystemCpuLoads = this.codec.decodeValues(valueBuffer, maxSystemCpuLoadEncodingStrategy, numValues);
        List<String> maxSysCpuAgentIds = this.codec.decodeValues(valueBuffer, maxSysCpuAgentIdEncodingStrategy, numValues);

        List<JoinStatBo> joinCpuLoadBoList = new ArrayList<JoinStatBo>(numValues);
        for (int i = 0; i < numValues; i++) {
            JoinCpuLoadBo joinCpuLoadBo = new JoinCpuLoadBo();
            joinCpuLoadBo.setId(id);
            joinCpuLoadBo.setTimestamp(timestamps.get(i));
            joinCpuLoadBo.setJvmCpuLoad(AgentStatUtils.convertLongToDouble(jvmCpuLoads.get(i)));
            joinCpuLoadBo.setMinJvmCpuLoad(AgentStatUtils.convertLongToDouble(minJvmCpuLoads.get(i)));
            joinCpuLoadBo.setMinJvmCpuAgentId(minJvmCpuAgentIds.get(i));
            joinCpuLoadBo.setMaxJvmCpuLoad(AgentStatUtils.convertLongToDouble(maxJvmCpuLoads.get(i)));
            joinCpuLoadBo.setMaxJvmCpuAgentId(maxJvmCpuAgentIds.get(i));
            joinCpuLoadBo.setSystemCpuLoad(AgentStatUtils.convertLongToDouble(systemCpuLoads.get(i)));
            joinCpuLoadBo.setMinSystemCpuLoad(AgentStatUtils.convertLongToDouble(minSystemCpuLoads.get(i)));
            joinCpuLoadBo.setMinSysCpuAgentId(minSysCpuAgentIds.get(i));
            joinCpuLoadBo.setMaxSystemCpuLoad(AgentStatUtils.convertLongToDouble(maxSystemCpuLoads.get(i)));
            joinCpuLoadBo.setMaxSysCpuAgentId(maxSysCpuAgentIds.get(i));
            joinCpuLoadBoList.add(joinCpuLoadBo);
        }
        return joinCpuLoadBoList;
    }
}
