/*
 * Copyright 2018 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Component("joinFileDescriptorCodec")
public class FileDescriptorCodec implements ApplicationStatCodec {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public FileDescriptorCodec(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "agentStatDataPointCodec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinFileDescriptorBoList) {
        if (CollectionUtils.isEmpty(joinFileDescriptorBoList)) {
            throw new IllegalArgumentException("fileDescriptorBoList must not be empty");
        }

        final int numValues = joinFileDescriptorBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder openFileDescriptorCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minOpenFileDescriptorCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minOpenFileDescriptorCountIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxOpenFileDescriptorCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxOpenFileDescriptorCountIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        for (JoinStatBo joinStatBo : joinFileDescriptorBoList) {
            JoinFileDescriptorBo joinFileDescriptorBo = (JoinFileDescriptorBo) joinStatBo;
            timestamps.add(joinFileDescriptorBo.getTimestamp());
            openFileDescriptorCountAnalyzerBuilder.addValue(joinFileDescriptorBo.getAvgOpenFDCount());
            minOpenFileDescriptorCountAnalyzerBuilder.addValue(joinFileDescriptorBo.getMinOpenFDCount());
            minOpenFileDescriptorCountIdAnalyzerBuilder.addValue(joinFileDescriptorBo.getMinOpenFDCountAgentId());
            maxOpenFileDescriptorCountAnalyzerBuilder.addValue(joinFileDescriptorBo.getMaxOpenFDCount());
            maxOpenFileDescriptorCountIdAnalyzerBuilder.addValue(joinFileDescriptorBo.getMaxOpenFDCountAgentId());

        }
        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, openFileDescriptorCountAnalyzerBuilder.build(), minOpenFileDescriptorCountAnalyzerBuilder.build(), minOpenFileDescriptorCountIdAnalyzerBuilder.build(), maxOpenFileDescriptorCountAnalyzerBuilder.build(), maxOpenFileDescriptorCountIdAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer,
                    StrategyAnalyzer<Long> openFileDescriptorCountAnalyzer,
                    StrategyAnalyzer<Long> minOpenFileDescriptorCountAnalyzer,
                    StrategyAnalyzer<String> minOpenFileDescriptorCountIdAnalyzer,
                    StrategyAnalyzer<Long> maxOpenFileDescriptorCountAnalyzer,
                    StrategyAnalyzer<String> maxOpenFileDescriptorCountIdAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(openFileDescriptorCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minOpenFileDescriptorCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minOpenFileDescriptorCountIdAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxOpenFileDescriptorCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxOpenFileDescriptorCountIdAnalyzer.getBestStrategy().getCode());

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, openFileDescriptorCountAnalyzer.getBestStrategy(), openFileDescriptorCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minOpenFileDescriptorCountAnalyzer.getBestStrategy(), minOpenFileDescriptorCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minOpenFileDescriptorCountIdAnalyzer.getBestStrategy(), minOpenFileDescriptorCountIdAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxOpenFileDescriptorCountAnalyzer.getBestStrategy(), maxOpenFileDescriptorCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxOpenFileDescriptorCountIdAnalyzer.getBestStrategy(), maxOpenFileDescriptorCountIdAnalyzer.getValues());

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
        EncodingStrategy<Long> openFileDescriptorCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> minOpenFileDescriptorCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minOpenFileDescriptorCountIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> maxOpenFileDescriptorCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxOpenFileDescriptorCountIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());


        // decode values
        List<Long> openFileDescriptorCounts = this.codec.decodeValues(valueBuffer, openFileDescriptorCountEncodingStrategy, numValues);
        List<Long> minOpenFileDescriptorCounts = this.codec.decodeValues(valueBuffer, minOpenFileDescriptorCountEncodingStrategy, numValues);
        List<String> minOpenFileDescriptorCountAgentIds = this.codec.decodeValues(valueBuffer, minOpenFileDescriptorCountIdEncodingStrategy, numValues);
        List<Long> maxOpenFileDescriptorCounts = this.codec.decodeValues(valueBuffer, maxOpenFileDescriptorCountEncodingStrategy, numValues);
        List<String> maxOpenFileDescriptorCountAgentIds = this.codec.decodeValues(valueBuffer, maxOpenFileDescriptorCountIdEncodingStrategy, numValues);


        List<JoinStatBo> joinFileDescriptorBoList = new ArrayList<JoinStatBo>(numValues);
        for (int i = 0; i < numValues; i++) {
            JoinFileDescriptorBo joinFileDescriptorBo = new JoinFileDescriptorBo();
            joinFileDescriptorBo.setId(id);
            joinFileDescriptorBo.setTimestamp(timestamps.get(i));
            joinFileDescriptorBo.setAvgOpenFDCount(openFileDescriptorCounts.get(i));
            joinFileDescriptorBo.setMinOpenFDCount(minOpenFileDescriptorCounts.get(i));
            joinFileDescriptorBo.setMinOpenFDCountAgentId(minOpenFileDescriptorCountAgentIds.get(i));
            joinFileDescriptorBo.setMaxOpenFDCount(maxOpenFileDescriptorCounts.get(i));
            joinFileDescriptorBo.setMaxOpenFDCountAgentId(maxOpenFileDescriptorCountAgentIds.get(i));
            joinFileDescriptorBoList.add(joinFileDescriptorBo);
        }
        return joinFileDescriptorBoList;
    }
}
