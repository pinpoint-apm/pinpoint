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

package com.navercorp.pinpoint.common.server.bo.codec.stat;

import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
public interface TestAgentStatDataPointFactory<T extends Number> {

    int MAX_NUM_TEST_VALUES = 20 + 1; // Random API's upper bound field is exclusive

    Random RANDOM = new Random();

    List<T> createConstantValues(T minValue, T maxValue);
    List<T> createConstantValues(T minValue, T maxValue, int numValues);
    List<T> createRandomValues(T minValue, T maxValue);
    List<T> createRandomValues(T minValue, T maxValue, int numValues);
    List<T> createIncreasingValues(T minValue, T maxValue, T minIncrement, T maxIncrement);
    List<T> createIncreasingValues(T minValue, T maxValue, T minIncrement, T maxIncrement, int numValues);
    List<T> createDecreasingValues(T minValue, T maxValue, T minDecrement, T maxDecrement);
    List<T> createDecreasingValues(T minValue, T maxValue, T minDecrement, T maxDecrement, int numValues);
    List<T> createFluctuatingValues(T minValue, T maxValue, T minFluctuation, T maxFluctuation);
    List<T> createFluctuatingValues(T minValue, T maxValue, T minFluctuation, T maxFluctuation, int numValues);

    TestAgentStatDataPointFactory<Short> SHORT = new TestAgentStatDataPointFactoryImpl<Short>() {
        @Override
        protected Short add(Short a, Short b) {
            return (short) (a.shortValue() + b.shortValue());
        }

        @Override
        protected Short diff(Short a, Short b) {
            return (short) (a.shortValue() - b.shortValue());
        }

        @Override
        protected Short createValue(Short minValue, Short maxValue) {
            short min = minValue;
            short max = maxValue;
            if (min > max) {
                throw new IllegalArgumentException("min is larger than max");
            } else if (min == max) {
                return min;
            } else if (min < 0 && max > 0) {
                short positiveRandom = (short) RANDOM.nextInt(max);
                short negativeRandom = (short) RANDOM.nextInt(Math.abs(min));
                if (RANDOM.nextInt(positiveRandom) > RANDOM.nextInt(negativeRandom)) {
                    return positiveRandom;
                } else {
                    return (short) (negativeRandom * -1);
                }
            } else {
                int value = RANDOM.nextInt(max - min);
                value += min;
                return (short) value;
            }
        }
    };

    TestAgentStatDataPointFactory<Integer> INTEGER = new TestAgentStatDataPointFactoryImpl<Integer>() {
        @Override
        protected Integer add(Integer a, Integer b) {
            return a.intValue() + b.intValue();
        }

        @Override
        protected Integer diff(Integer a, Integer b) {
            return a.intValue() - b.intValue();
        }

        @Override
        protected Integer createValue(Integer minValue, Integer maxValue) {
            int min = minValue;
            int max = maxValue;
            if (min > max) {
                throw new IllegalArgumentException("min is larger than max");
            } else if (min == max) {
                return min;
            } else if (min < 0 && max > 0) {
                int positiveRandom = RANDOM.nextInt(max);
                int negativeRandom = RANDOM.nextInt(Math.abs(min));
                if (RANDOM.nextInt(positiveRandom) > RANDOM.nextInt(negativeRandom)) {
                    return positiveRandom;
                } else {
                    return negativeRandom;
                }
            } else {
                int value = RANDOM.nextInt(max - min);
                value += min;
                return value;
            }
        }
    };

    TestAgentStatDataPointFactory<Long> LONG = new TestAgentStatDataPointFactoryImpl<Long>() {

        @Override
        protected Long add(Long a, Long b) {
            return a.longValue() + b.longValue();
        }

        @Override
        protected Long diff(Long a, Long b) {
            return a.longValue() - b.longValue();
        }

        @Override
        protected Long createValue(Long minValue, Long maxValue) {
            long min = minValue;
            long max = maxValue;
            if (min > max) {
                throw new IllegalArgumentException("min is larger than max");
            } else if (min == max) {
                return min;
            } else if (min < 0 && max > 0) {
                long positiveRandom = getUnsignedRandom() % max;
                long negativeRandom = getUnsignedRandom() % Math.abs(min);
                if (getUnsignedRandom() % positiveRandom > getUnsignedRandom() % negativeRandom) {
                    return positiveRandom;
                } else {
                    return negativeRandom * -1;
                }
            } else {
                long value = getUnsignedRandom();
                value %= max - min;
                value += min;
                return value;
            }
        }

        private long getUnsignedRandom() {
            return Math.abs(RANDOM.nextLong());
        }
    };

    abstract class TestAgentStatDataPointFactoryImpl<T extends Number> implements TestAgentStatDataPointFactory<T> {

        protected abstract T add(T a, T b);
        protected abstract T diff(T a, T b);
        protected abstract T createValue(T minValue, T maxValue);

        @Override
        public List<T> createConstantValues(T minValue, T maxValue) {
            final int numValues = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
            return this.createConstantValues(minValue, maxValue, numValues);
        }

        @Override
        public List<T> createConstantValues(T minValue, T maxValue, int numValues) {
            T value = this.createValue(minValue, maxValue);
            List<T> values = new ArrayList<T>(numValues);
            for (int i = 0; i < numValues; i++) {
                values.add(value);
            }
            return values;
        }

        @Override
        public List<T> createRandomValues(T minValue, T maxValue) {
            final int numValues = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
            return this.createRandomValues(minValue, maxValue, numValues);
        }

        @Override
        public List<T> createRandomValues(T minValue, T maxValue, int numValues) {
            List<T> values = new ArrayList<T>(numValues);
            for (int i = 0; i < numValues; i++) {
                T value = this.createValue(minValue, maxValue);
                values.add(value);
            }
            return values;
        }

        @Override
        public List<T> createIncreasingValues(T minValue, T maxValue, T minIncrement, T maxIncrement) {
            final int numValues = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
            return this.createIncreasingValues(minValue, maxValue, minIncrement, maxIncrement, numValues);
        }

        @Override
        public List<T> createIncreasingValues(T minValue, T maxValue, T minIncrement, T maxIncrement, int numValues) {
            List<T> values = new ArrayList<T>(numValues);
            T value = this.createValue(minValue, maxValue);
            values.add(value);
            for (int i = 0; i < numValues - 1; i++) {
                T increment = this.createValue(minIncrement, maxIncrement);
                value = add(value, increment);
                values.add(value);
            }
            return values;
        }

        @Override
        public List<T> createDecreasingValues(T minValue, T maxValue, T minDecrement, T maxDecrement) {
            final int numValues = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
            return this.createDecreasingValues(minValue, maxValue, minDecrement, maxDecrement, numValues);
        }

        @Override
        public List<T> createDecreasingValues(T minValue, T maxValue, T minDecrement, T maxDecrement, int numValues) {
            List<T> values = new ArrayList<T>(numValues);
            T value = this.createValue(minValue, maxValue);
            values.add(value);
            for (int i = 0; i < numValues - 1; i++) {
                T decrement = this.createValue(minDecrement, maxDecrement);
                value = diff(value, decrement);
                values.add(value);
            }
            return values;
        }

        @Override
        public List<T> createFluctuatingValues(T minValue, T maxValue, T minFluctuation, T maxFluctuation) {
            final int numValues = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
            return this.createFluctuatingValues(minValue, maxValue, minFluctuation, maxFluctuation, numValues);
        }

        @Override
        public List<T> createFluctuatingValues(T minValue, T maxValue, T minFluctuation, T maxFluctuation, int numValues) {
            List<T> values = new ArrayList<T>(numValues);
            T value = this.createValue(minValue, maxValue);
            values.add(value);
            boolean sign = RANDOM.nextBoolean();
            for (int i = 0; i < numValues - 1; i++) {
                T fluctuation = this.createValue(minFluctuation, maxFluctuation);
                // randomly add or substract fluctuation
                if (sign) {
                    value = add(value, fluctuation);
                } else {
                    value = diff(value, fluctuation);
                }
                values.add(value);
            }
            return values;
        }
    }
}
