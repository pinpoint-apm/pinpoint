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

package com.navercorp.pinpoint.common.server.bo.codec.strategy;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
public abstract class EncodingStrategyTestBase {

    protected static final int NUM_TEST_VALUES = 20;

    private static final Random RANDOM = new Random();

    public <T> void testStrategy(List<T> expectedValues, EncodingStrategy<T> strategy) {
        Buffer encodeBuffer = new AutomaticBuffer();
        strategy.encodeValues(encodeBuffer, expectedValues);
        Buffer decodeBuffer = new FixedBuffer(encodeBuffer.getBuffer());
        List<T> actualValues = strategy.decodeValues(decodeBuffer, expectedValues.size());
        Assert.assertEquals(expectedValues, actualValues);
    }

    public List<Short> createRandomShorts(int numTestValues, boolean unsigned) {
        List<Short> values = new ArrayList<Short>(numTestValues);
        for (int i = 0; i < numTestValues; ++i) {
            if (unsigned) {
                values.add((short) RANDOM.nextInt(Short.MAX_VALUE));
            } else {
                boolean positive = RANDOM.nextBoolean();
                values.add((short) (RANDOM.nextInt(Short.MAX_VALUE) * (positive ? 1 : -1)));
            }
        }
        return values;
    }

    public List<Integer> createRandomIntegers(int numTestValues, boolean unsigned) {
        List<Integer> values = new ArrayList<Integer>(numTestValues);
        for (int i = 0; i < numTestValues; ++i) {
            if (unsigned) {
                values.add(RANDOM.nextInt(Integer.MAX_VALUE));
            } else {
                values.add(RANDOM.nextInt());
            }
        }
        return values;
    }

    public List<Long> createRandomLongs(int numTestValues, boolean unsigned) {
        List<Long> values = new ArrayList<Long>(numTestValues);
        for (int i = 0; i < numTestValues; ++i) {
            long value = RANDOM.nextLong();
            if (unsigned) {
                value = Math.abs(value);
            }
            values.add(value);
        }
        return values;
    }
}
