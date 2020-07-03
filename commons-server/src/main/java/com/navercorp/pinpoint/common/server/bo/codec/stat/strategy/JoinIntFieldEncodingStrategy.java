/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.codec.stat.strategy;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
public class JoinIntFieldEncodingStrategy implements JoinEncodingStrategy<JoinIntFieldBo> {

    private final EncodingStrategy<Integer> avgValueStrategy;
    private final EncodingStrategy<Integer> minValueStrategy;
    private final EncodingStrategy<String> minAgentIdStrategy;
    private final EncodingStrategy<Integer> maxValueStrategy;
    private final EncodingStrategy<String> maxAgentIdStrategy;


    public JoinIntFieldEncodingStrategy(EncodingStrategy<Integer> avgValueStrategy, EncodingStrategy<Integer> minValueStrategy, EncodingStrategy<String> minAgentIdStrategy, EncodingStrategy<Integer> maxValueStrategy, EncodingStrategy<String> maxAgentIdStrategy) {
        this.avgValueStrategy = Objects.requireNonNull(avgValueStrategy, "avgValueStrategy");
        this.minValueStrategy = Objects.requireNonNull(minValueStrategy, "minValueStrategy");
        this.minAgentIdStrategy = Objects.requireNonNull(minAgentIdStrategy, "minAgentIdStrategy");
        this.maxValueStrategy = Objects.requireNonNull(maxValueStrategy, "maxValueStrategy");
        this.maxAgentIdStrategy = Objects.requireNonNull(maxAgentIdStrategy, "maxAgentIdStrategy");
    }

    @Override
    @Deprecated
    public byte getCode() {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public byte[] getCodes() {
        byte[] codes = new byte[5];
        codes[0] = avgValueStrategy.getCode();
        codes[1] = minValueStrategy.getCode();
        codes[2] = minAgentIdStrategy.getCode();
        codes[3] = maxValueStrategy.getCode();
        codes[4] = maxAgentIdStrategy.getCode();
        return codes;
    }

    @Override
    public void encodeValues(Buffer buffer, List<JoinIntFieldBo> values) {
        final List<Integer> avgValues = values.stream().map(e -> e.getAvg()).collect(Collectors.toList());
        avgValueStrategy.encodeValues(buffer, avgValues);

        final List<Integer> minValues = values.stream().map(e -> e.getMin()).collect(Collectors.toList());
        minValueStrategy.encodeValues(buffer, minValues);

        final List<String> minAgentIds = values.stream().map(e -> e.getMinAgentId()).collect(Collectors.toList());
        minAgentIdStrategy.encodeValues(buffer, minAgentIds);

        final List<Integer> maxValues = values.stream().map(e -> e.getMax()).collect(Collectors.toList());
        maxValueStrategy.encodeValues(buffer, maxValues);

        final List<String> maxAgentIds = values.stream().map(e -> e.getMaxAgentId()).collect(Collectors.toList());
        maxAgentIdStrategy.encodeValues(buffer, maxAgentIds);
    }

    public static JoinIntFieldEncodingStrategy getFromCode(int[] codes) {
        Objects.requireNonNull(codes, "codes");

        if (codes.length != 5) {
            throw new IllegalArgumentException("codes must be 5 size");
        }

        return getFromCode(codes[0], codes[1], codes[2], codes[3], codes[4]);
    }

    public static JoinIntFieldEncodingStrategy getFromCode(int code1, int code2, int code3, int code4, int code5) {
        final UnsignedIntegerEncodingStrategy avgValueStrategy = UnsignedIntegerEncodingStrategy.getFromCode(code1);
        final UnsignedIntegerEncodingStrategy minValueStrategy = UnsignedIntegerEncodingStrategy.getFromCode(code2);
        final StringEncodingStrategy minAgentIdStrategy = StringEncodingStrategy.getFromCode(code3);
        final UnsignedIntegerEncodingStrategy maxValueStrategy = UnsignedIntegerEncodingStrategy.getFromCode(code4);
        final StringEncodingStrategy maxAgentIdStrategy = StringEncodingStrategy.getFromCode(code5);

        return new JoinIntFieldEncodingStrategy(avgValueStrategy, minValueStrategy, minAgentIdStrategy, maxValueStrategy, maxAgentIdStrategy);
    }

    @Override
    public List<JoinIntFieldBo> decodeValues(Buffer buffer, int numValues) {

        final List<Integer> avgValueList = avgValueStrategy.decodeValues(buffer, numValues);
        final List<Integer> minValueList = minValueStrategy.decodeValues(buffer, numValues);
        final List<String> minAgentIdList = minAgentIdStrategy.decodeValues(buffer, numValues);
        final List<Integer> maxValueList = maxValueStrategy.decodeValues(buffer, numValues);
        final List<String> maxAgentIdList = maxAgentIdStrategy.decodeValues(buffer, numValues);

        List<JoinIntFieldBo> result = new ArrayList<>(numValues);

        for (int i = 0; i < numValues; i++) {
            final Integer avgValue = avgValueList.get(i);
            final Integer minValue = minValueList.get(i);
            final String minAgentId = minAgentIdList.get(i);
            final Integer maxValue = maxValueList.get(i);
            final String maxAgentId = maxAgentIdList.get(i);

            final JoinIntFieldBo joinIntFieldBo = new JoinIntFieldBo(avgValue, minValue, minAgentId, maxValue, maxAgentId);

            result.add(joinIntFieldBo);
        }

        return Collections.unmodifiableList(result);
    }

}
