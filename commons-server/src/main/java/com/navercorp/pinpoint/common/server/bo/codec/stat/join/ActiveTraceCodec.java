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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.JoinIntFieldEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.JoinIntFieldStrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedShortEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Component("joinActiveTraceCodec")
public class ActiveTraceCodec implements ApplicationStatCodec {
    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public ActiveTraceCodec(AgentStatDataPointCodec codec) {
        this.codec = codec;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinActiveTraceBoList) {
        if (CollectionUtils.isEmpty(joinActiveTraceBoList)) {
            throw new IllegalArgumentException("JoinActiveTraceBoList must not be empty");
        }

        final int numValues = joinActiveTraceBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedShortEncodingStrategy.Analyzer.Builder versionAnalyzerBuilder = new UnsignedShortEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder schemaTypeAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        JoinIntFieldStrategyAnalyzer.Builder totalCountAnalyzerBuilder = new JoinIntFieldStrategyAnalyzer.Builder();

        for (JoinStatBo joinStatBo : joinActiveTraceBoList) {
            JoinActiveTraceBo joinActiveTraceBo = (JoinActiveTraceBo) joinStatBo;
            timestamps.add(joinActiveTraceBo.getTimestamp());
            versionAnalyzerBuilder.addValue(joinActiveTraceBo.getVersion());
            schemaTypeAnalyzerBuilder.addValue(joinActiveTraceBo.getHistogramSchemaType());
            totalCountAnalyzerBuilder.addValue(joinActiveTraceBo.getTotalCountJoinValue());
        }

        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, versionAnalyzerBuilder.build(), schemaTypeAnalyzerBuilder.build(), totalCountAnalyzerBuilder.build());

    }

    private void encodeDataPoints(Buffer valueBuffer, StrategyAnalyzer<Short> versionAnalyzer, StrategyAnalyzer<Integer> schemaTypeAnalyzer, JoinIntFieldStrategyAnalyzer totalCountAnalyzer) {
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(versionAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(schemaTypeAnalyzer.getBestStrategy().getCode());

        final byte[] codes = totalCountAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        this.codec.encodeValues(valueBuffer, versionAnalyzer.getBestStrategy(), versionAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, schemaTypeAnalyzer.getBestStrategy(), schemaTypeAnalyzer.getValues());
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

        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        EncodingStrategy<Short> versionEncodingStrategy = UnsignedShortEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> schemaTypeEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        JoinIntFieldEncodingStrategy totalCountJoinIntValueEncodingStrategy = JoinIntFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());

        List<Short> versionList = this.codec.decodeValues(valueBuffer, versionEncodingStrategy, numValues);
        List<Integer> schemaTypeList = this.codec.decodeValues(valueBuffer, schemaTypeEncodingStrategy, numValues);
        List<JoinIntFieldBo> totalCountJoinIntValueList = this.codec.decodeValues(valueBuffer, totalCountJoinIntValueEncodingStrategy, numValues);

        List<JoinStatBo> joinActiveTraceBoList = new ArrayList<JoinStatBo>();
        for (int i = 0; i < numValues; i++) {
            JoinActiveTraceBo joinActiveTraceBo = new JoinActiveTraceBo();
            joinActiveTraceBo.setId(id);
            joinActiveTraceBo.setVersion(versionList.get(i));
            joinActiveTraceBo.setTimestamp(timestampList.get(i));
            joinActiveTraceBo.setHistogramSchemaType(schemaTypeList.get(i));
            joinActiveTraceBo.setTotalCountJoinValue(totalCountJoinIntValueList.get(i));
            joinActiveTraceBoList.add(joinActiveTraceBo);
        }

        return joinActiveTraceBoList;
    }

}
