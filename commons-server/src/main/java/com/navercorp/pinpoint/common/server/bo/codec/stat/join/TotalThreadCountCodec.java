/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StringEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("joinTotalThreadCountCodec")
public class TotalThreadCountCodec implements ApplicationStatCodec {
    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public TotalThreadCountCodec(AgentStatDataPointCodec codec) { this.codec = codec; }

    @Override
    public byte getVersion() { return VERSION; }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinTotalThreadCountBoList) {
        if (CollectionUtils.isEmpty(joinTotalThreadCountBoList)) {
            throw new IllegalArgumentException("joinTotalThreadCountBoList must not be empty");
        }
        final int numValues = joinTotalThreadCountBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        JoinLongFieldStrategyAnalyzer.Builder totalThreadCountAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();

        for (JoinStatBo joinStatBo : joinTotalThreadCountBoList) {
            JoinTotalThreadCountBo joinTotalThreadCountBo = (JoinTotalThreadCountBo) joinStatBo;
            timestamps.add(joinTotalThreadCountBo.getTimestamp());
            totalThreadCountAnalyzerBuilder.addValue(joinTotalThreadCountBo.getTotalThreadCountJoinValue());

        }
        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, totalThreadCountAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer, JoinLongFieldStrategyAnalyzer totalThreadCountAnalyzer) {
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();

        final byte[] codes = totalThreadCountAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        this.codec.encodeValues(valueBuffer, totalThreadCountAnalyzer.getBestStrategy(), totalThreadCountAnalyzer.getValues());
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
        JoinLongFieldEncodingStrategy totalThreadCountEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());

        final List<JoinLongFieldBo> totalThreadCountList = this.codec.decodeValues(valueBuffer, totalThreadCountEncodingStrategy, numValues);

        List<JoinStatBo> joinTotalThreadCountBoList = new ArrayList<JoinStatBo>();
        for (int i = 0 ; i < numValues ; i++) {
            JoinTotalThreadCountBo joinTotalThreadCountBo = new JoinTotalThreadCountBo();
            joinTotalThreadCountBo.setId(id);
            joinTotalThreadCountBo.setTimestamp(timestampList.get(i));
            joinTotalThreadCountBo.setTotalThreadCountJoinValue(totalThreadCountList.get(i));
            joinTotalThreadCountBoList.add(joinTotalThreadCountBo);
        }

        return joinTotalThreadCountBoList;
    }
}
