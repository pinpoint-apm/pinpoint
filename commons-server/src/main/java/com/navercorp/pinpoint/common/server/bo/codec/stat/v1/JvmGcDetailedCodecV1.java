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

    private final HeaderCodecV1<Long> longHeaderCodec;

    @Autowired
    public JvmGcDetailedCodecV1(AgentStatDataPointCodec codec, HeaderCodecV1<Long> longHeaderCodec) {
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
    public void encodeValues(Buffer valueBuffer, List<JvmGcDetailedBo> jvmGcDetailedBos) {
        if (CollectionUtils.isEmpty(jvmGcDetailedBos)) {
            throw new IllegalArgumentException("jvmGcDetailedBos must not be empty");
        }
        final int numValues = jvmGcDetailedBos.size();
        valueBuffer.putVInt(numValues);

        List<Long> timestamps = new ArrayList<>(numValues);
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
        int header = 0;
        int position = 0;
        header = this.longHeaderCodec.encodeHeader(header, position, gcNewCountStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, gcNewTimeStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, codeCacheUsedStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, newGenUsedStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, oldGenUsedStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, survivorSpaceUsedStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, permGenUsedStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, metaspaceUsedStrategyAnalyzer.getBestStrategy());
        valueBuffer.putVInt(header);
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
        int header = valueBuffer.readVInt();
        int position = 0;
        EncodingStrategy<Long> gcNewCountEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> gcNewTimeEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> codeCacheUsedEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> newGenUsedEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> oldGenUsedEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> survivorSpaceUsedEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> permGenUsedEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> metaspaceUsedEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        // decode values
        List<Long> gcNewCounts = this.codec.decodeValues(valueBuffer, gcNewCountEncodingStrategy, numValues);
        List<Long> gcNewTimes = this.codec.decodeValues(valueBuffer, gcNewTimeEncodingStrategy, numValues);
        List<Long> codeCacheUseds = this.codec.decodeValues(valueBuffer, codeCacheUsedEncodingStrategy, numValues);
        List<Long> newGenUseds = this.codec.decodeValues(valueBuffer, newGenUsedEncodingStrategy, numValues);
        List<Long> oldGenUseds = this.codec.decodeValues(valueBuffer, oldGenUsedEncodingStrategy, numValues);
        List<Long> survivorSpaceUseds = this.codec.decodeValues(valueBuffer, survivorSpaceUsedEncodingStrategy, numValues);
        List<Long> permGenUseds = this.codec.decodeValues(valueBuffer, permGenUsedEncodingStrategy, numValues);
        List<Long> metaspaceUseds = this.codec.decodeValues(valueBuffer, metaspaceUsedEncodingStrategy, numValues);

        List<JvmGcDetailedBo> jvmGcDetailedBos = new ArrayList<>(numValues);
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
