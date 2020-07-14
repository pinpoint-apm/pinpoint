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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("joinLoadedClassCodec")
public class LoadedClassCodec implements ApplicationStatCodec {
    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    public LoadedClassCodec(AgentStatDataPointCodec codec) { this.codec = codec; }

    @Override
    public byte getVersion() { return VERSION; }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinStatBoList) {
        if (CollectionUtils.isEmpty(joinStatBoList)) {
            throw new IllegalArgumentException("directBufferBoList must not be empty");
        }

        final int numValues = joinStatBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        JoinLongFieldStrategyAnalyzer.Builder loadedClassAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();
        JoinLongFieldStrategyAnalyzer.Builder unloadedClassAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();

        for (JoinStatBo joinStatBo : joinStatBoList) {
            JoinLoadedClassBo joinLoadedClassBo = (JoinLoadedClassBo) joinStatBo;
            timestamps.add(joinLoadedClassBo.getTimestamp());
            loadedClassAnalyzerBuilder.addValue(joinLoadedClassBo.getLoadedClassJoinValue());
            unloadedClassAnalyzerBuilder.addValue(joinLoadedClassBo.getUnloadedClassJoinValue());
        }

        codec.encodeTimestamps(valueBuffer, timestamps);

        encodeDataPoints(valueBuffer, loadedClassAnalyzerBuilder.build(), unloadedClassAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer, JoinLongFieldStrategyAnalyzer loadedClassAnalyzer, JoinLongFieldStrategyAnalyzer unloadedClassAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();

        byte[] codes = loadedClassAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }
        codes = unloadedClassAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }


        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, loadedClassAnalyzer.getBestStrategy(), loadedClassAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, unloadedClassAnalyzer.getBestStrategy(), unloadedClassAnalyzer.getValues());
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
        JoinLongFieldEncodingStrategy loadedClassEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());
        JoinLongFieldEncodingStrategy unloadedClassEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());

        // decode values
        List<JoinLongFieldBo> loadedClassList = this.codec.decodeValues(valueBuffer, loadedClassEncodingStrategy, numValues);
        List<JoinLongFieldBo> unloadedClassList = this.codec.decodeValues(valueBuffer, unloadedClassEncodingStrategy, numValues);

        List<JoinStatBo> joinLoadedClassBoList = new ArrayList<JoinStatBo>(numValues);
        for (int i = 0; i < numValues; i++) {
            JoinLoadedClassBo joinLoadedClassBo = new JoinLoadedClassBo();
            joinLoadedClassBo.setId(id);
            joinLoadedClassBo.setTimestamp(timestamps.get(i));

            joinLoadedClassBo.setLoadedClassJoinValue(loadedClassList.get(i));
            joinLoadedClassBo.setUnloadedClassJoinValue(unloadedClassList.get(i));

            joinLoadedClassBoList.add(joinLoadedClassBo);
        }
        return joinLoadedClassBoList;
    }
}
