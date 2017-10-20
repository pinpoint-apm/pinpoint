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

package com.navercorp.pinpoint.common.server.bo.codec.strategy.impl;

import com.navercorp.pinpoint.common.server.bo.codec.TypedBufferHandler;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategyTestBase;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ValueEncodingStrategyTest extends EncodingStrategyTestBase {

    @Test
    public void unsigned_shorts_should_be_encoded_and_decoded_correctly() {
        int numTestValues = NUM_TEST_VALUES;
        List<Short> expectedValues = createRandomShorts(numTestValues, true);
        ValueEncodingStrategy.Unsigned<Short> strategy = new ValueEncodingStrategy.Unsigned<Short>(TypedBufferHandler.SHORT_BUFFER_HANDLER);
        testStrategy(expectedValues, strategy);
    }

    @Test
    public void empty_unsigned_shorts_should_be_encoded_and_decoded_correctly() {
        List<Short> expectedValues = Collections.emptyList();
        ValueEncodingStrategy.Unsigned<Short> strategy = new ValueEncodingStrategy.Unsigned<Short>(TypedBufferHandler.SHORT_BUFFER_HANDLER);
        testStrategy(expectedValues, strategy);
    }

    @Test
    public void unsigned_ints_should_be_encoded_and_decoded_correctly() {
        int numTestValues = NUM_TEST_VALUES;
        List<Integer> expectedValues = createRandomIntegers(numTestValues, true);
        ValueEncodingStrategy.Unsigned<Integer> strategy = new ValueEncodingStrategy.Unsigned<Integer>(TypedBufferHandler.INTEGER_BUFFER_HANDLER);
        testStrategy(expectedValues, strategy);
    }

    @Test
    public void empty_unsigned_ints_should_be_encoded_and_decoded_correctly() {
        List<Integer> expectedValues = Collections.emptyList();
        ValueEncodingStrategy.Unsigned<Integer> strategy = new ValueEncodingStrategy.Unsigned<Integer>(TypedBufferHandler.INTEGER_BUFFER_HANDLER);
        testStrategy(expectedValues, strategy);
    }

    @Test
    public void unsigned_longs_should_be_encoded_and_decoded_correctly() {
        int numTestValues = NUM_TEST_VALUES;
        List<Long> expectedValues = createRandomLongs(numTestValues, true);
        ValueEncodingStrategy.Unsigned<Long> strategy = new ValueEncodingStrategy.Unsigned<Long>(TypedBufferHandler.LONG_BUFFER_HANDLER);
        testStrategy(expectedValues, strategy);
    }

    @Test
    public void empty_unsigned_longs_should_be_encoded_and_decoded_correctly() {
        List<Long> expectedValues = Collections.emptyList();
        ValueEncodingStrategy.Unsigned<Long> strategy = new ValueEncodingStrategy.Unsigned<Long>(TypedBufferHandler.LONG_BUFFER_HANDLER);
        testStrategy(expectedValues, strategy);
    }
}
