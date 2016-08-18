/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v1;

import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.UnsignedShortEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
public class HeaderCodecV1Test {

    private static final int MAX_NUM_COPIES = 3;
    private static final Random RANDOM = new Random();

    @Test
    public void shortHeaderCodecTest() {
        HeaderCodecV1.ShortHeaderCodecV1 codec = new HeaderCodecV1.ShortHeaderCodecV1();
        testCodec(codec, UnsignedShortEncodingStrategy.values());
    }

    @Test
    public void integerHeaderCodecTest() {
        HeaderCodecV1.IntegerHeaderCodecV1 codec = new HeaderCodecV1.IntegerHeaderCodecV1();
        testCodec(codec, UnsignedIntegerEncodingStrategy.values());
    }

    @Test
    public void longHeaderCodecTest() {
        HeaderCodecV1.LongHeaderCodecV1 codec = new HeaderCodecV1.LongHeaderCodecV1();
        testCodec(codec, UnsignedLongEncodingStrategy.values());
    }

    private <T extends Number> void testCodec(HeaderCodecV1<T> codec, EncodingStrategy<T>... strategies) {
        List<EncodingStrategy<T>> expectedStrategies = randomizeEncodingStrategies(strategies);
        int header = 0;
        int position = 0;
        for (EncodingStrategy<T> strategy : expectedStrategies) {
            header = codec.encodeHeader(header, position, strategy);
            position += codec.getHeaderBitSize();
        }
        // When
        position = 0;
        List<EncodingStrategy<T>> actualStrategies = new ArrayList<>(expectedStrategies.size());
        for (int i = 0; i < expectedStrategies.size(); ++i) {
            actualStrategies.add(codec.decodeHeader(header, position));
            position += codec.getHeaderBitSize();
        }
        // Then
        Assert.assertEquals(expectedStrategies, actualStrategies);
    }

    private <T extends Number> List<EncodingStrategy<T>> randomizeEncodingStrategies(EncodingStrategy<T>... strategies) {
        if (strategies == null || strategies.length == 0) {
            return Collections.emptyList();
        }
        int numCopies = RANDOM.nextInt(MAX_NUM_COPIES) + 1;
        List<EncodingStrategy<T>> shuffledStrategies = new ArrayList<>(strategies.length * numCopies);
        for (int i = 0; i < numCopies; ++i) {
            for (EncodingStrategy<T> strategy : strategies) {
                shuffledStrategies.add(strategy);
            }
        }
        Collections.shuffle(shuffledStrategies);
        return shuffledStrategies;
    }
}
