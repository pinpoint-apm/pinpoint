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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Component("joinTransactionCodec")
public class TransactionCodec implements ApplicationStatCodec {
    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public TransactionCodec(AgentStatDataPointCodec codec) {
        Assert.notNull(codec, "agentStatDataPointCodec must not be null");
        this.codec = codec;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinTransactionBoList) {
        if (CollectionUtils.isEmpty(joinTransactionBoList)) {
            throw new IllegalArgumentException("joinTransactionBoList must not be empty");
        }

        final int numValues = joinTransactionBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder collectIntervalAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder totalCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minTotalCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minTotalCountAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxTotalCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxTotalCountAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        for (JoinStatBo joinStatBo : joinTransactionBoList) {
            JoinTransactionBo joinTransactionBo = (JoinTransactionBo) joinStatBo;
            timestamps.add(joinTransactionBo.getTimestamp());
            collectIntervalAnalyzerBuilder.addValue(joinTransactionBo.getCollectInterval());
            totalCountAnalyzerBuilder.addValue(joinTransactionBo.getTotalCount());
            minTotalCountAnalyzerBuilder.addValue(joinTransactionBo.getMinTotalCount());
            minTotalCountAgentIdAnalyzerBuilder.addValue(joinTransactionBo.getMinTotalCountAgentId());
            maxTotalCountAnalyzerBuilder.addValue(joinTransactionBo.getMaxTotalCount());
            maxTotalCountAgentIdAnalyzerBuilder.addValue(joinTransactionBo.getMaxTotalCountAgentId());
        }

        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, collectIntervalAnalyzerBuilder.build(), totalCountAnalyzerBuilder.build(), minTotalCountAnalyzerBuilder.build(), minTotalCountAgentIdAnalyzerBuilder.build(), maxTotalCountAnalyzerBuilder.build(), maxTotalCountAgentIdAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer, StrategyAnalyzer<Long> collectIntervalAnalyzerBuilder, StrategyAnalyzer<Long> totalCountAnalyzerBuilder, StrategyAnalyzer<Long> minTotalCountAnalyzerBuilder, StrategyAnalyzer<String> minTotalCountAgentIdAnalyzerBuilder, StrategyAnalyzer<Long> maxTotalCountAnalyzerBuilder, StrategyAnalyzer<String> maxTotalCountAgentIdAnalyzerBuilder) {
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(collectIntervalAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(totalCountAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(minTotalCountAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(minTotalCountAgentIdAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(maxTotalCountAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(maxTotalCountAgentIdAnalyzerBuilder.getBestStrategy().getCode());
        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        this.codec.encodeValues(valueBuffer, collectIntervalAnalyzerBuilder.getBestStrategy(), collectIntervalAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, totalCountAnalyzerBuilder.getBestStrategy(), totalCountAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, minTotalCountAnalyzerBuilder.getBestStrategy(), minTotalCountAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, minTotalCountAgentIdAnalyzerBuilder.getBestStrategy(), minTotalCountAgentIdAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, maxTotalCountAnalyzerBuilder.getBestStrategy(), maxTotalCountAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, maxTotalCountAgentIdAnalyzerBuilder.getBestStrategy(), maxTotalCountAgentIdAnalyzerBuilder.getValues());
    }


    @Override
    public List<JoinStatBo> decodeValues(Buffer valueBuffer, ApplicationStatDecodingContext decodingContext) {
        final String id = decodingContext.getApplicationId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> timestampList = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        EncodingStrategy<Long> collectIntervalEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> totalCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> minTotalCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minTotalCountAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> maxTotalCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxTotalCountAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        List<Long> collectIntervalList = this.codec.decodeValues(valueBuffer, collectIntervalEncodingStrategy, numValues);
        List<Long> totalCountList = this.codec.decodeValues(valueBuffer, totalCountEncodingStrategy, numValues);
        List<Long> minTotalCountList = this.codec.decodeValues(valueBuffer, minTotalCountEncodingStrategy, numValues);
        List<String> minTotalCountAgentIdList = this.codec.decodeValues(valueBuffer, minTotalCountAgentIdEncodingStrategy, numValues);
        List<Long> maxTotalCountList = this.codec.decodeValues(valueBuffer, maxTotalCountEncodingStrategy, numValues);
        List<String> maxTotalCountAgentIdList = this.codec.decodeValues(valueBuffer, maxTotalCountAgentIdEncodingStrategy, numValues);

        List<JoinStatBo> joinTransactionBoList = new ArrayList<JoinStatBo>();
        for (int i = 0 ; i < numValues ; ++i) {
            JoinTransactionBo joinTransactionBo = new JoinTransactionBo();
            joinTransactionBo.setId(id);
            joinTransactionBo.setTimestamp(timestampList.get(i));
            joinTransactionBo.setCollectInterval(collectIntervalList.get(i));
            joinTransactionBo.setTotalCount(totalCountList.get(i));
            joinTransactionBo.setMinTotalCount(minTotalCountList.get(i));
            joinTransactionBo.setMinTotalCountAgentId(minTotalCountAgentIdList.get(i));
            joinTransactionBo.setMaxTotalCount(maxTotalCountList.get(i));
            joinTransactionBo.setMaxTotalCountAgentId(maxTotalCountAgentIdList.get(i));
            joinTransactionBoList.add(joinTransactionBo);
        }

        return joinTransactionBoList;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }
}
