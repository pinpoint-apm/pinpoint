/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.linechart;

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

    public static DownSampler<Double> getDoubleDownSampler(double defaultValue, int scale) {
        return new DoubleDownSampler(defaultValue, scale);
    }

    private static abstract class AbstractDownSampler<T extends Number> implements DownSampler<T> {

        protected final T defaultValue;

        private AbstractDownSampler(T defaultValue) {
            this.defaultValue = defaultValue;
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
        public Integer sampleAvg(Collection<Integer> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            double avg = 0;
            int cnt = 1;
            for (int value : values) {
                avg += (value - avg) / cnt;
                ++cnt;
            }
            return (int) Math.round(avg);
        }

        @Override
        public Integer sampleMax(Collection<Integer> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            return Collections.max(values);
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
        public Long sampleAvg(Collection<Long> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            double avg = 0;
            int cnt = 1;
            for (long value : values) {
                avg += (value - avg) / cnt;
                ++cnt;
            }
            return Math.round(avg);
        }

        @Override
        public Long sampleMax(Collection<Long> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            return Collections.max(values);
        }
    }

    private static class DoubleDownSampler extends AbstractDownSampler<Double> {

        private static final int DEFAULT_SCALE = 2;
        private final int scale;

        private DoubleDownSampler(Double defaultValue) {
            this(defaultValue, DEFAULT_SCALE);
        }

        private DoubleDownSampler(Double defaultValue, int scale) {
            super(defaultValue);
            this.scale = scale;
        }

        @Override
        public Double sampleMin(Collection<Double> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            return roundToScale(Collections.min(values));
        }

        @Override
        public Double sampleAvg(Collection<Double> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            double avg = 0;
            int cnt = 1;
            for (double value : values) {
                avg += (value - avg) / cnt;
                ++cnt;
            }
            return roundToScale(avg);
        }

        @Override
        public Double sampleMax(Collection<Double> values) {
            if (CollectionUtils.isEmpty(values)) {
                return this.defaultValue;
            }
            return roundToScale(Collections.max(values));
        }

        private double roundToScale(double value) {
            return new BigDecimal(value).setScale(this.scale, RoundingMode.HALF_UP).doubleValue();
        }
    }

}
