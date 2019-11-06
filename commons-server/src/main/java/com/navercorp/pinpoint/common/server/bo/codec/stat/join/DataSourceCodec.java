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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedShortEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
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
@Component("joinDataSourceCodec")
public class DataSourceCodec implements ApplicationStatCodec {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public DataSourceCodec(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "agentStatDataPointCodec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> JoinStatBoList) {
        if (CollectionUtils.isEmpty(JoinStatBoList)) {
            throw new IllegalArgumentException("joinDataSourceListBoList must not be empty");
        }
        final List<JoinDataSourceListBo> joinDataSourceListBoList = castJoinDataSourceListBoList(JoinStatBoList);
        valueBuffer.putVInt(joinDataSourceListBoList.size());
        encodeTimestamps(valueBuffer, joinDataSourceListBoList);
        encodeJoinDataSourceListBo(valueBuffer, joinDataSourceListBoList);
    }

    private void encodeJoinDataSourceListBo(Buffer valueBuffer, List<JoinDataSourceListBo> joinDataSourceListBoList) {
        for (JoinDataSourceListBo joinDataSourceListBo : joinDataSourceListBoList) {
            encodeJoinDataSourceBo(valueBuffer, joinDataSourceListBo.getJoinDataSourceBoList());
        }
    }

    private void encodeJoinDataSourceBo(Buffer valueBuffer, List<JoinDataSourceBo> joinDataSourceBoList) {
        final int numValues = joinDataSourceBoList.size();
        valueBuffer.putVInt(numValues);

        UnsignedShortEncodingStrategy.Analyzer.Builder serviceTypeAnalyzerBuilder = new UnsignedShortEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder jdbcUrlAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder avgActiveConnectionSizeAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder minActiveConnectionSizeAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minActiveConnectionAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder maxActiveConnectionSizeAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxActiveConnectionAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        for (JoinDataSourceBo joinDataSourceBo : joinDataSourceBoList) {
            serviceTypeAnalyzerBuilder.addValue(joinDataSourceBo.getServiceTypeCode());
            jdbcUrlAnalyzerBuilder.addValue(joinDataSourceBo.getUrl());
            avgActiveConnectionSizeAnalyzerBuilder.addValue(joinDataSourceBo.getAvgActiveConnectionSize());
            minActiveConnectionSizeAnalyzerBuilder.addValue(joinDataSourceBo.getMinActiveConnectionSize());
            minActiveConnectionAgentIdAnalyzerBuilder.addValue(joinDataSourceBo.getMinActiveConnectionAgentId());
            maxActiveConnectionSizeAnalyzerBuilder.addValue(joinDataSourceBo.getMaxActiveConnectionSize());
            maxActiveConnectionAgentIdAnalyzerBuilder.addValue(joinDataSourceBo.getMaxActiveConnectionAgentId());
        }

        StrategyAnalyzer<Short> serviceTypeAnalyzer = serviceTypeAnalyzerBuilder.build();
        StrategyAnalyzer<String> jdbcUrlAnalyzer = jdbcUrlAnalyzerBuilder.build();
        StrategyAnalyzer<Integer> avgActiveConnectionSizeAnalyzer = avgActiveConnectionSizeAnalyzerBuilder.build();
        StrategyAnalyzer<Integer> minActiveConnectionSizeAnalyzer = minActiveConnectionSizeAnalyzerBuilder.build();
        StrategyAnalyzer<String> minActiveConnectionAgentIdAnalyzer = minActiveConnectionAgentIdAnalyzerBuilder.build();
        StrategyAnalyzer<Integer> maxActiveConnectionSizeAnalyzer = maxActiveConnectionSizeAnalyzerBuilder.build();
        StrategyAnalyzer<String> maxActiveConnectionAgentIdAnalyzer = maxActiveConnectionAgentIdAnalyzerBuilder.build();

        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(serviceTypeAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(jdbcUrlAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(avgActiveConnectionSizeAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minActiveConnectionSizeAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minActiveConnectionAgentIdAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxActiveConnectionSizeAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxActiveConnectionAgentIdAnalyzer.getBestStrategy().getCode());

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        this.codec.encodeValues(valueBuffer, serviceTypeAnalyzer.getBestStrategy(), serviceTypeAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, jdbcUrlAnalyzer.getBestStrategy(), jdbcUrlAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, avgActiveConnectionSizeAnalyzer.getBestStrategy(), avgActiveConnectionSizeAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minActiveConnectionSizeAnalyzer.getBestStrategy(), minActiveConnectionSizeAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minActiveConnectionAgentIdAnalyzer.getBestStrategy(), minActiveConnectionAgentIdAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxActiveConnectionSizeAnalyzer.getBestStrategy(), maxActiveConnectionSizeAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxActiveConnectionAgentIdAnalyzer.getBestStrategy(), maxActiveConnectionAgentIdAnalyzer.getValues());
    }

    private void encodeTimestamps(Buffer valueBuffer, List<JoinDataSourceListBo> joinDataSourceListBoList) {
        List<Long> timestamps = new ArrayList<Long>(joinDataSourceListBoList.size());

        for (JoinDataSourceListBo joinDataSourceListBo : joinDataSourceListBoList) {
            timestamps.add(joinDataSourceListBo.getTimestamp());
        }

        codec.encodeTimestamps(valueBuffer, timestamps);
    }

    private List<JoinDataSourceListBo> castJoinDataSourceListBoList(List<JoinStatBo> joinStatBoList) {
        List<JoinDataSourceListBo> joinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>();

        for (JoinStatBo joinStatBo : joinStatBoList) {
            joinDataSourceListBoList.add((JoinDataSourceListBo)joinStatBo);
        }

        return joinDataSourceListBoList;
    }

    @Override
    public List<JoinStatBo> decodeValues(Buffer valueBuffer, ApplicationStatDecodingContext decodingContext) {
        final String id = decodingContext.getApplicationId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> timestampList = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        List<JoinStatBo> joinDataSourceListBoList = new ArrayList<JoinStatBo>(numValues);

        for (int i = 0; i < numValues; ++i) {
            JoinDataSourceListBo joinDataSourceListBo = new JoinDataSourceListBo();
            joinDataSourceListBo.setId(id);
            joinDataSourceListBo.setTimestamp(timestampList.get(i));
            joinDataSourceListBo.setJoinDataSourceBoList(decodeJoinDataSourceBoList(valueBuffer));
            joinDataSourceListBoList.add(joinDataSourceListBo);
        }

        return joinDataSourceListBoList;
    }

    private List<JoinDataSourceBo> decodeJoinDataSourceBoList(Buffer valueBuffer) {
        int numValues = valueBuffer.readVInt();
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);

        EncodingStrategy<Short> serviceTypeEncodingStrategy = UnsignedShortEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> urlEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> avgActiveConnectionSizeEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> minActiveConnectionSizeEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minActiveConnectionAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> maxActiveConnectionSizeEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxActiveConnectionAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        List<Short> serviceTypeCodeList = this.codec.decodeValues(valueBuffer, serviceTypeEncodingStrategy, numValues);
        List<String> jdbcUrlList = this.codec.decodeValues(valueBuffer, urlEncodingStrategy, numValues);
        List<Integer> avgActiveConnectionSizeCountList = this.codec.decodeValues(valueBuffer, avgActiveConnectionSizeEncodingStrategy, numValues);
        List<Integer> minActiveConnectionSizeCountList = this.codec.decodeValues(valueBuffer, minActiveConnectionSizeEncodingStrategy, numValues);
        List<String> minActiveConnectionAgentIdList = this.codec.decodeValues(valueBuffer, minActiveConnectionAgentIdEncodingStrategy, numValues);
        List<Integer> maxActiveConnectionSizeList = this.codec.decodeValues(valueBuffer, maxActiveConnectionSizeEncodingStrategy, numValues);
        List<String> maxActiveConnectionAgentIdList = this.codec.decodeValues(valueBuffer, maxActiveConnectionAgentIdEncodingStrategy, numValues);

        List<JoinDataSourceBo> joinDataSourceBoList = new ArrayList<JoinDataSourceBo>(numValues);

        for (int i = 0; i < numValues; ++i) {
            JoinDataSourceBo joinDataSourceBo = new JoinDataSourceBo();
            joinDataSourceBo.setServiceTypeCode(serviceTypeCodeList.get(i));
            joinDataSourceBo.setUrl(jdbcUrlList.get(i));
            joinDataSourceBo.setAvgActiveConnectionSize(avgActiveConnectionSizeCountList.get(i));
            joinDataSourceBo.setMinActiveConnectionSize(minActiveConnectionSizeCountList.get(i));
            joinDataSourceBo.setMinActiveConnectionAgentId(minActiveConnectionAgentIdList.get(i));
            joinDataSourceBo.setMaxActiveConnectionSize(maxActiveConnectionSizeList.get(i));
            joinDataSourceBo.setMaxActiveConnectionAgentId(maxActiveConnectionAgentIdList.get(i));
            joinDataSourceBoList.add(joinDataSourceBo);
        }

        return joinDataSourceBoList;
    }


}
