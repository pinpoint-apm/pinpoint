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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.JoinLongFieldEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.JoinLongFieldStrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Component("joinTransactionCodec")
public class TransactionCodec implements ApplicationStatCodec {
    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public TransactionCodec(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "agentStatDataPointCodec");
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
        JoinLongFieldStrategyAnalyzer.Builder totalCountAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();

        for (JoinStatBo joinStatBo : joinTransactionBoList) {
            JoinTransactionBo joinTransactionBo = (JoinTransactionBo) joinStatBo;
            timestamps.add(joinTransactionBo.getTimestamp());
            collectIntervalAnalyzerBuilder.addValue(joinTransactionBo.getCollectInterval());
            totalCountAnalyzerBuilder.addValue(joinTransactionBo.getTotalCountJoinValue());
        }

        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, collectIntervalAnalyzerBuilder.build(), totalCountAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer, StrategyAnalyzer<Long> collectIntervalAnalyzer, JoinLongFieldStrategyAnalyzer totalCountAnalyzer) {
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(collectIntervalAnalyzer.getBestStrategy().getCode());

        final byte[] codes = totalCountAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        this.codec.encodeValues(valueBuffer, collectIntervalAnalyzer.getBestStrategy(), collectIntervalAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, totalCountAnalyzer.getBestStrategy(), totalCountAnalyzer.getValues());
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

        JoinLongFieldEncodingStrategy totalCountEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());

        List<Long> collectIntervalList = this.codec.decodeValues(valueBuffer, collectIntervalEncodingStrategy, numValues);
        final List<JoinLongFieldBo> totalCountList = this.codec.decodeValues(valueBuffer, totalCountEncodingStrategy, numValues);

        List<JoinStatBo> joinTransactionBoList = new ArrayList<JoinStatBo>();
        for (int i = 0 ; i < numValues ; i++) {
            JoinTransactionBo joinTransactionBo = new JoinTransactionBo();
            joinTransactionBo.setId(id);
            joinTransactionBo.setTimestamp(timestampList.get(i));
            joinTransactionBo.setCollectInterval(collectIntervalList.get(i));
            joinTransactionBo.setTotalCountJoinValue(totalCountList.get(i));
            joinTransactionBoList.add(joinTransactionBo);
        }

        return joinTransactionBoList;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }
}
