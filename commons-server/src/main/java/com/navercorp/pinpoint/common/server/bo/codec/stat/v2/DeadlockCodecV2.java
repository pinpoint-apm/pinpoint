/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Component("deadlockCodecV2")
public class DeadlockCodecV2 implements AgentStatCodec<DeadlockBo> {

    private static final byte VERSION = 2;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public DeadlockCodecV2(AgentStatDataPointCodec codec) {
        Assert.notNull(codec, "agentStatDataPointCodec must not be null");
        this.codec = codec;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<DeadlockBo> deadlockBos) {
        if (CollectionUtils.isEmpty(deadlockBos)) {
            throw new IllegalArgumentException("deadlockBos must not be empty");
        }
        final int numValues = deadlockBos.size();
        valueBuffer.putVInt(numValues);

        List<Long> startTimestamps = new ArrayList<Long>(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedIntegerEncodingStrategy.Analyzer.Builder deadlockedThreadCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        for (DeadlockBo deadlockBo : deadlockBos) {
            startTimestamps.add(deadlockBo.getStartTimestamp());
            timestamps.add(deadlockBo.getTimestamp());

            int deadlockedThreadCount = deadlockBo.getDeadlockedThreadCount();
            deadlockedThreadCountAnalyzerBuilder.addValue(deadlockedThreadCount);
        }

        this.codec.encodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, startTimestamps);
        this.codec.encodeTimestamps(valueBuffer, timestamps);
        this.encodeDataPoints(valueBuffer, deadlockedThreadCountAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer, StrategyAnalyzer<Integer> deadlockedThreadIdAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(deadlockedThreadIdAnalyzer.getBestStrategy().getCode());

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        // encode values
        this.codec.encodeValues(valueBuffer, deadlockedThreadIdAnalyzer.getBestStrategy(), deadlockedThreadIdAnalyzer.getValues());
    }

    @Override
    public List<DeadlockBo> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        final String agentId = decodingContext.getAgentId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> startTimestamps = this.codec.decodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, numValues);
        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        EncodingStrategy<Integer> deadlockedThreadCountEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());

        // decode values
        List<Integer> deadlockedThreadCountList = codec.decodeValues(valueBuffer, deadlockedThreadCountEncodingStrategy, numValues);

        List<DeadlockBo> deadlockBos = new ArrayList<DeadlockBo>(numValues);
        for (int i = 0; i < numValues; ++i) {
            DeadlockBo deadlockBo = new DeadlockBo();
            deadlockBo.setAgentId(agentId);
            deadlockBo.setStartTimestamp(startTimestamps.get(i));
            deadlockBo.setTimestamp(timestamps.get(i));
            deadlockBo.setDeadlockedThreadCount(deadlockedThreadCountList.get(i));

            deadlockBos.add(deadlockBo);
        }
        return deadlockBos;
    }

}
