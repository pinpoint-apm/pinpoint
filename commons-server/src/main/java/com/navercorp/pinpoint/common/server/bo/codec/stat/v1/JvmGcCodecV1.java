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
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component("jvmGcCodecV1")
public class JvmGcCodecV1 implements AgentStatCodec<JvmGcBo> {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    private final HeaderCodecV1<Long> longHeaderCodec;

    @Autowired
    public JvmGcCodecV1(AgentStatDataPointCodec codec, HeaderCodecV1<Long> longHeaderCodec) {
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
    public void encodeValues(Buffer valueBuffer, List<JvmGcBo> jvmGcBos) {
        if (CollectionUtils.isEmpty(jvmGcBos)) {
            throw new IllegalArgumentException("jvmGcBos must not be empty");
        }
        final int numValues = jvmGcBos.size();
        valueBuffer.putVInt(numValues);

        List<Long> timestamps = new ArrayList<>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder heapUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder heapMaxAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder nonHeapUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder nonHeapMaxAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder gcOldCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder gcOldTimeAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        for (JvmGcBo jvmGcBo : jvmGcBos) {
            timestamps.add(jvmGcBo.getTimestamp());
            heapUsedAnalyzerBuilder.addValue(jvmGcBo.getHeapUsed());
            heapMaxAnalyzerBuilder.addValue(jvmGcBo.getHeapMax());
            nonHeapUsedAnalyzerBuilder.addValue(jvmGcBo.getNonHeapUsed());
            nonHeapMaxAnalyzerBuilder.addValue(jvmGcBo.getNonHeapMax());
            gcOldCountAnalyzerBuilder.addValue(jvmGcBo.getGcOldCount());
            gcOldTimeAnalyzerBuilder.addValue(jvmGcBo.getGcOldTime());
        }

        this.codec.encodeTimestamps(valueBuffer, timestamps);
        this.encodeDataPoints(
                valueBuffer,
                heapUsedAnalyzerBuilder.build(),
                heapMaxAnalyzerBuilder.build(),
                nonHeapUsedAnalyzerBuilder.build(),
                nonHeapMaxAnalyzerBuilder.build(),
                gcOldCountAnalyzerBuilder.build(),
                gcOldTimeAnalyzerBuilder.build());
    }

    private void encodeDataPoints(
            Buffer valueBuffer,
            StrategyAnalyzer<Long> heapUsedStrategyAnalyzer,
            StrategyAnalyzer<Long> heapMaxStrategyAnalyzer,
            StrategyAnalyzer<Long> nonHeapUsedStrategyAnalyzer,
            StrategyAnalyzer<Long> nonHeapMaxStrategyAnalyzer,
            StrategyAnalyzer<Long> gcOldCountStrategyAnalyzer,
            StrategyAnalyzer<Long> gcOldTimeStrategyAnalyzer) {
        // encode header
        int header = 0;
        int position = 0;
        header = this.longHeaderCodec.encodeHeader(header, position, heapUsedStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, heapMaxStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, nonHeapUsedStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, nonHeapMaxStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, gcOldCountStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, gcOldTimeStrategyAnalyzer.getBestStrategy());
        valueBuffer.putVInt(header);
        // encode values
        this.codec.encodeValues(valueBuffer, heapUsedStrategyAnalyzer.getBestStrategy(), heapUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, heapMaxStrategyAnalyzer.getBestStrategy(), heapMaxStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, nonHeapUsedStrategyAnalyzer.getBestStrategy(), nonHeapUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, nonHeapMaxStrategyAnalyzer.getBestStrategy(), nonHeapMaxStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, gcOldCountStrategyAnalyzer.getBestStrategy(), gcOldCountStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, gcOldTimeStrategyAnalyzer.getBestStrategy(), gcOldTimeStrategyAnalyzer.getValues());
    }

    @Override
    public List<JvmGcBo> decodeValues(Buffer valueBuffer, long initialTimestamp) {
        int numValues = valueBuffer.readVInt();

        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        int header = valueBuffer.readVInt();
        int position = 0;
        EncodingStrategy<Long> heapUsedEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> heapMaxEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> nonHeapUsedEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> nonHeapMaxEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> gcOldCountEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> gcOldTimeEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        // decode values
        List<Long> heapUseds = this.codec.decodeValues(valueBuffer, heapUsedEncodingStrategy, numValues);
        List<Long> heapMaxes = this.codec.decodeValues(valueBuffer, heapMaxEncodingStrategy, numValues);
        List<Long> nonHeapUseds = this.codec.decodeValues(valueBuffer, nonHeapUsedEncodingStrategy, numValues);
        List<Long> nonHeapMaxes = this.codec.decodeValues(valueBuffer, nonHeapMaxEncodingStrategy, numValues);
        List<Long> gcOldCounts = this.codec.decodeValues(valueBuffer, gcOldCountEncodingStrategy,  numValues);
        List<Long> gcOldTimes = this.codec.decodeValues(valueBuffer, gcOldTimeEncodingStrategy, numValues);

        List<JvmGcBo> jvmGcBos = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; ++i) {
            JvmGcBo jvmGcBo = new JvmGcBo();
            jvmGcBo.setTimestamp(timestamps.get(i));
            jvmGcBo.setHeapUsed(heapUseds.get(i));
            jvmGcBo.setHeapMax(heapMaxes.get(i));
            jvmGcBo.setNonHeapUsed(nonHeapUseds.get(i));
            jvmGcBo.setNonHeapMax(nonHeapMaxes.get(i));
            jvmGcBo.setGcOldCount(gcOldCounts.get(i));
            jvmGcBo.setGcOldTime(gcOldTimes.get(i));
            jvmGcBos.add(jvmGcBo);
        }
        return jvmGcBos;
    }
}
