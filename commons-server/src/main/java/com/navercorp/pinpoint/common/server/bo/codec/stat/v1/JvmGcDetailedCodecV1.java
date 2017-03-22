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
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component("jvmGcDetailedCodecV1")
public class JvmGcDetailedCodecV1 implements AgentStatCodec<JvmGcDetailedBo> {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public JvmGcDetailedCodecV1(AgentStatDataPointCodec codec) {
        Assert.notNull(codec, "agentStatDataPointCodec must not be null");
        this.codec = codec;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JvmGcDetailedBo> jvmGcDetailedBos) {
        if (CollectionUtils.isEmpty(jvmGcDetailedBos)) {
            throw new IllegalArgumentException("jvmGcDetailedBos must not be empty");
        }
        final int numValues = jvmGcDetailedBos.size();
        valueBuffer.putVInt(numValues);

        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder gcNewCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder gcNewTimeAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder codeCacheUsedStrategyAnalyzer = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder newGenUsedStrategyAnalyzer = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder oldGenUsedStrategyAnalyzer = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder survivorSpaceUsedStrategyAnalyzer = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder permGenUsedStrategyAnalyzer = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder metaspaceUsedStrategyAnalyzer = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        for (JvmGcDetailedBo jvmGcDetailedBo : jvmGcDetailedBos) {
            timestamps.add(jvmGcDetailedBo.getTimestamp());
            gcNewCountAnalyzerBuilder.addValue(jvmGcDetailedBo.getGcNewCount());
            gcNewTimeAnalyzerBuilder.addValue(jvmGcDetailedBo.getGcNewTime());
            codeCacheUsedStrategyAnalyzer.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getCodeCacheUsed()));
            newGenUsedStrategyAnalyzer.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getNewGenUsed()));
            oldGenUsedStrategyAnalyzer.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getOldGenUsed()));
            survivorSpaceUsedStrategyAnalyzer.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getSurvivorSpaceUsed()));
            permGenUsedStrategyAnalyzer.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getPermGenUsed()));
            metaspaceUsedStrategyAnalyzer.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getMetaspaceUsed()));
        }
        this.codec.encodeTimestamps(valueBuffer, timestamps);
        this.encodeDataPoints(
                valueBuffer,
                gcNewCountAnalyzerBuilder.build(),
                gcNewTimeAnalyzerBuilder.build(),
                codeCacheUsedStrategyAnalyzer.build(),
                newGenUsedStrategyAnalyzer.build(),
                oldGenUsedStrategyAnalyzer.build(),
                survivorSpaceUsedStrategyAnalyzer.build(),
                permGenUsedStrategyAnalyzer.build(),
                metaspaceUsedStrategyAnalyzer.build());
    }

    private void encodeDataPoints(
            Buffer valueBuffer,
            StrategyAnalyzer<Long> gcNewCountStrategyAnalyzer,
            StrategyAnalyzer<Long> gcNewTimeStrategyAnalyzer,
            StrategyAnalyzer<Long> codeCacheUsedStrategyAnalyzer,
            StrategyAnalyzer<Long> newGenUsedStrategyAnalyzer,
            StrategyAnalyzer<Long> oldGenUsedStrategyAnalyzer,
            StrategyAnalyzer<Long> survivorSpaceUsedStrategyAnalyzer,
            StrategyAnalyzer<Long> permGenUsedStrategyAnalyzer,
            StrategyAnalyzer<Long> metaspaceUsedStrategyAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(gcNewCountStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(gcNewTimeStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(codeCacheUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(newGenUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(oldGenUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(survivorSpaceUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(permGenUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(metaspaceUsedStrategyAnalyzer.getBestStrategy().getCode());
        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, gcNewCountStrategyAnalyzer.getBestStrategy(), gcNewCountStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, gcNewTimeStrategyAnalyzer.getBestStrategy(), gcNewTimeStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, codeCacheUsedStrategyAnalyzer.getBestStrategy(), codeCacheUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, newGenUsedStrategyAnalyzer.getBestStrategy(), newGenUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, oldGenUsedStrategyAnalyzer.getBestStrategy(), oldGenUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, survivorSpaceUsedStrategyAnalyzer.getBestStrategy(), survivorSpaceUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, permGenUsedStrategyAnalyzer.getBestStrategy(), permGenUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, metaspaceUsedStrategyAnalyzer.getBestStrategy(), metaspaceUsedStrategyAnalyzer.getValues());
    }

    @Override
    public List<JvmGcDetailedBo> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        final String agentId = decodingContext.getAgentId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        EncodingStrategy<Long> gcNewCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> gcNewTimeEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> codeCacheUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> newGenUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> oldGenUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> survivorSpaceUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> permGenUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> metaspaceUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        // decode values
        List<Long> gcNewCounts = this.codec.decodeValues(valueBuffer, gcNewCountEncodingStrategy, numValues);
        List<Long> gcNewTimes = this.codec.decodeValues(valueBuffer, gcNewTimeEncodingStrategy, numValues);
        List<Long> codeCacheUseds = this.codec.decodeValues(valueBuffer, codeCacheUsedEncodingStrategy, numValues);
        List<Long> newGenUseds = this.codec.decodeValues(valueBuffer, newGenUsedEncodingStrategy, numValues);
        List<Long> oldGenUseds = this.codec.decodeValues(valueBuffer, oldGenUsedEncodingStrategy, numValues);
        List<Long> survivorSpaceUseds = this.codec.decodeValues(valueBuffer, survivorSpaceUsedEncodingStrategy, numValues);
        List<Long> permGenUseds = this.codec.decodeValues(valueBuffer, permGenUsedEncodingStrategy, numValues);
        List<Long> metaspaceUseds = this.codec.decodeValues(valueBuffer, metaspaceUsedEncodingStrategy, numValues);

        List<JvmGcDetailedBo> jvmGcDetailedBos = new ArrayList<JvmGcDetailedBo>(numValues);
        for (int i = 0; i < numValues; ++i) {
            JvmGcDetailedBo jvmGcDetailedBo = new JvmGcDetailedBo();
            jvmGcDetailedBo.setAgentId(agentId);
            jvmGcDetailedBo.setTimestamp(timestamps.get(i));
            jvmGcDetailedBo.setGcNewCount(gcNewCounts.get(i));
            jvmGcDetailedBo.setGcNewTime(gcNewTimes.get(i));
            jvmGcDetailedBo.setCodeCacheUsed(AgentStatUtils.convertLongToDouble(codeCacheUseds.get(i)));
            jvmGcDetailedBo.setNewGenUsed(AgentStatUtils.convertLongToDouble(newGenUseds.get(i)));
            jvmGcDetailedBo.setOldGenUsed(AgentStatUtils.convertLongToDouble(oldGenUseds.get(i)));
            jvmGcDetailedBo.setSurvivorSpaceUsed(AgentStatUtils.convertLongToDouble(survivorSpaceUseds.get(i)));
            jvmGcDetailedBo.setPermGenUsed(AgentStatUtils.convertLongToDouble(permGenUseds.get(i)));
            jvmGcDetailedBo.setMetaspaceUsed(AgentStatUtils.convertLongToDouble(metaspaceUseds.get(i)));
            jvmGcDetailedBos.add(jvmGcDetailedBo);
        }
        return jvmGcDetailedBos;
    }
}
