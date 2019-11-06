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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StringEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedShortEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Component("dataSourceCodecV2")
public class DataSourceCodecV2 implements AgentStatCodec<DataSourceListBo> {

    private static final byte VERSION = 2;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public DataSourceCodecV2(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "agentStatDataPointCodec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<DataSourceListBo> dataSourceListBos) {
        if (CollectionUtils.isEmpty(dataSourceListBos)) {
            throw new IllegalArgumentException("dataSourceListBos must not be empty");
        }
        final int numValues = dataSourceListBos.size();
        valueBuffer.putVInt(numValues);

        for (DataSourceListBo dataSourceListBo : dataSourceListBos) {
            encodeDataSourceListBo(valueBuffer, dataSourceListBo);
        }
    }

    private void encodeDataSourceListBo(Buffer valueBuffer, DataSourceListBo dataSourceListBo) {
        final int numValues = dataSourceListBo.size();
        valueBuffer.putVInt(numValues);

        if (numValues == 0) {
            return;
        }

        // id                   // int
        // serviceTypeCode      // short
        // name                 // string
        // jdbcUrl              // string
        // activeConnectionSize //int
        // maxConnectionSize    // int
        List<Long> startTimestamps = new ArrayList<Long>(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);

        UnsignedIntegerEncodingStrategy.Analyzer.Builder idAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        UnsignedShortEncodingStrategy.Analyzer.Builder serviceTypeAnalyzerBuilder = new UnsignedShortEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder databaseNameAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder jdbcUrlAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder activeConnectionSizeAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder maxConnectionSizeAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();

        for (DataSourceBo dataSourceBo : dataSourceListBo.getList()) {
            startTimestamps.add(dataSourceBo.getStartTimestamp());
            timestamps.add(dataSourceBo.getTimestamp());

            idAnalyzerBuilder.addValue(dataSourceBo.getId());
            serviceTypeAnalyzerBuilder.addValue(dataSourceBo.getServiceTypeCode());
            databaseNameAnalyzerBuilder.addValue(dataSourceBo.getDatabaseName());
            jdbcUrlAnalyzerBuilder.addValue(dataSourceBo.getJdbcUrl());
            activeConnectionSizeAnalyzerBuilder.addValue(dataSourceBo.getActiveConnectionSize());
            maxConnectionSizeAnalyzerBuilder.addValue(dataSourceBo.getMaxConnectionSize());
        }
        this.codec.encodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, startTimestamps);
        this.codec.encodeTimestamps(valueBuffer, timestamps);
        this.encodeDataPoints(valueBuffer, idAnalyzerBuilder.build(), serviceTypeAnalyzerBuilder.build(),
                databaseNameAnalyzerBuilder.build(), jdbcUrlAnalyzerBuilder.build(),
                activeConnectionSizeAnalyzerBuilder.build(), maxConnectionSizeAnalyzerBuilder.build());

    }

    private void encodeDataPoints(Buffer valueBuffer, StrategyAnalyzer<Integer> idAnalyzerBuilder, StrategyAnalyzer<Short> serviceTypeAnalyzerBuilder,
                                  StrategyAnalyzer<String> databaseNameAnalyzerBuilder, StrategyAnalyzer<String> jdbcUrlAnalyzerBuilder,
                                  StrategyAnalyzer<Integer> activeConnectionSizeAnalyzerBuilder, StrategyAnalyzer<Integer> maxConnectionSizeAnalyzerBuilder) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(idAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(serviceTypeAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(databaseNameAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(jdbcUrlAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(activeConnectionSizeAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(maxConnectionSizeAnalyzerBuilder.getBestStrategy().getCode());

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        // encode values
        this.codec.encodeValues(valueBuffer, idAnalyzerBuilder.getBestStrategy(), idAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, serviceTypeAnalyzerBuilder.getBestStrategy(), serviceTypeAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, databaseNameAnalyzerBuilder.getBestStrategy(), databaseNameAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, jdbcUrlAnalyzerBuilder.getBestStrategy(), jdbcUrlAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, activeConnectionSizeAnalyzerBuilder.getBestStrategy(), activeConnectionSizeAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, maxConnectionSizeAnalyzerBuilder.getBestStrategy(), maxConnectionSizeAnalyzerBuilder.getValues());
    }

    @Override
    public List<DataSourceListBo> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        int numValues = valueBuffer.readVInt();

        List<DataSourceListBo> dataSourceListBos = new ArrayList<DataSourceListBo>(numValues);
        for (int i = 0; i < numValues; i++) {
            DataSourceListBo dataSourceListBo = decodeValue(valueBuffer, decodingContext);
            dataSourceListBos.add(dataSourceListBo);
        }
        return dataSourceListBos;
    }

    private DataSourceListBo decodeValue(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
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

        EncodingStrategy<Integer> idEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Short> serviceTypeEncodingStrategy = UnsignedShortEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> databaseNameEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> urlEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> activeConnectionSizeStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> maxConnectionSizeStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());

        List<Integer> ids = this.codec.decodeValues(valueBuffer, idEncodingStrategy, numValues);
        List<Short> serviceTypeCodes = this.codec.decodeValues(valueBuffer, serviceTypeEncodingStrategy, numValues);
        List<String> databaseNames = this.codec.decodeValues(valueBuffer, databaseNameEncodingStrategy, numValues);
        List<String> jdbcUrls = this.codec.decodeValues(valueBuffer, urlEncodingStrategy, numValues);
        List<Integer> activeConnectionSizes = this.codec.decodeValues(valueBuffer, activeConnectionSizeStrategy, numValues);
        List<Integer> maxConnectionSizes = this.codec.decodeValues(valueBuffer, maxConnectionSizeStrategy, numValues);

        DataSourceListBo dataSourceListBo = new DataSourceListBo();
        for (int i = 0; i < numValues; i++) {
            if (i == 0) {
                dataSourceListBo.setAgentId(agentId);
                dataSourceListBo.setTimestamp(timestamps.get(i));
                dataSourceListBo.setStartTimestamp(startTimestamps.get(i));
            }

            DataSourceBo dataSourceBo = new DataSourceBo();
            dataSourceBo.setAgentId(agentId);
            dataSourceBo.setStartTimestamp(startTimestamps.get(i));
            dataSourceBo.setTimestamp(timestamps.get(i));

            dataSourceBo.setId(ids.get(i));
            dataSourceBo.setServiceTypeCode(serviceTypeCodes.get(i));
            dataSourceBo.setDatabaseName(databaseNames.get(i));
            dataSourceBo.setJdbcUrl(jdbcUrls.get(i));
            dataSourceBo.setActiveConnectionSize(activeConnectionSizes.get(i));
            dataSourceBo.setMaxConnectionSize(maxConnectionSizes.get(i));
            dataSourceListBo.add(dataSourceBo);
        }
        return dataSourceListBo;
    }

}
