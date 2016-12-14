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

package com.navercorp.pinpoint.web.vo.stat.chart;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;

/**
 * Down samples consecutive data points, such as a time-series dataset.
 *
 * @author harebox
 * @author HyunGil Jeong
 */
public class DownSamplers {

    private DownSamplers() {
    }

    public static DownSampler<Integer> getIntegerDownSampler(int defaultValue) {
        return new IntegerDownSampler(defaultValue);
    }

    public static DownSampler<Long> getLongDownSampler(long defaultValue) {
        return new LongDownSampler(defaultValue);
    }

    public static DownSampler<Double> getDoubleDownSampler(double defaultValue) {
        return new DoubleDownSampler(defaultValue);
    }

    public static DownSampler<Double> getDoubleDownSampler(double defaultValue, int numDecimals) {
        return new DoubleDownSampler(defaultValue, numDecimals);
    }

    private static abstract class AbstractDownSampler<T extends Number> implements DownSampler<T> {

        protected final T defaultValue;

        private AbstractDownSampler(T defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public T getDefaultValue() {
            return this.defaultValue;
        }

        @Override
        public double sampleAvg(Collection<T> values, int numDecimals) {
            return roundToScale(sampleAvg(values), numDecimals);
        }

        protected final double roundToScale(double value, int numDecimals) {
            return BigDecimal.valueOf(value).setScale(numDecimals, RoundingMode.HALF_UP).doubleValue();
        }
    }

    private static class IntegerDownSampler extends AbstractDownSampler<Integer> {

        private IntegerDownSampler(Integer defaultValue) {
            super(defaultValue);
        }

        @Override
        public Integer sampleMin(Collection<Integer> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            return Collections.min(values);
        }

        @Override
        public double sampleAvg(Collection<Integer> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            double avg = 0;
            int cnt = 1;
            for (int value : values) {
                avg += (value - avg) / cnt;
                ++cnt;
            }
            return avg;
        }

        @Override
        public Integer sampleMax(Collection<Integer> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            return Collections.max(values);
        }

        @Override
        public Integer sampleSum(Collection<Integer> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            int sum = 0;
            for (int value : values) {
                int newSum = sum + value;
                // Checks integer overflow - from JDK8 Math.addExact(int, int)
                if (((sum ^ newSum) & (value ^ newSum)) < 0) {
                    return Integer.MAX_VALUE;
                }
                sum = newSum;
            }
            return sum;
        }
    }

    private static class LongDownSampler extends AbstractDownSampler<Long> {

        private LongDownSampler(Long defaultValue) {
            super(defaultValue);
        }

        @Override
        public Long sampleMin(Collection<Long> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            return Collections.min(values);
        }

        @Override
        public double sampleAvg(Collection<Long> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            double avg = 0;
            int cnt = 1;
            for (long value : values) {
                avg += (value - avg) / cnt;
                ++cnt;
            }
            return avg;
        }

        @Override
        public Long sampleMax(Collection<Long> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            return Collections.max(values);
        }

        @Override
        public Long sampleSum(Collection<Long> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            long sum = 0L;
            for (long value : values) {
                long newSum = sum + value;
                // Checks long overflow - from JDK8 Math.addExact(long, long)
                if (((sum ^ newSum) & (value ^ newSum)) < 0) {
                    return Long.MAX_VALUE;
                }
                sum = newSum;
            }
            return sum;
        }
    }

    private static class DoubleDownSampler extends AbstractDownSampler<Double> {

        private final Integer numDecimals;

        private DoubleDownSampler(Double defaultValue) {
            super(defaultValue);
            this.numDecimals = null;
        }

        private DoubleDownSampler(Double defaultValue, int numDecimals) {
            super(defaultValue);
            this.numDecimals = numDecimals;
        }

        @Override
        public Double sampleMin(Collection<Double> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            double min = Collections.min(values);
            if (this.numDecimals == null) {
                return min;
            } else {
                return roundToScale(min, this.numDecimals);
            }
        }

        @Override
        public double sampleAvg(Collection<Double> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            double avg = getAvg(values);
            if (this.numDecimals == null) {
                return avg;
            } else {
                return roundToScale(avg, this.numDecimals);
            }
        }

        @Override
        public double sampleAvg(Collection<Double> values, int numDecimals) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            double avg = getAvg(values);
            return roundToScale(avg, numDecimals);
        }

        private double getAvg(Collection<Double> values) {
            double avg = 0;
            int cnt = 1;
            for (double value : values) {
                avg += (value - avg) / cnt;
                ++cnt;
            }
            return avg;
        }

        @Override
        public Double sampleMax(Collection<Double> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            double max = Collections.max(values);
            if (this.numDecimals == null) {
                return max;
            } else {
                return roundToScale(max, this.numDecimals);
            }
        }

        @Override
        public Double sampleSum(Collection<Double> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            double sum = 0;
            for (double value : values) {
                sum += value;
            }
            return sum;
        }

    }

}
