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

import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;
import static org.apache.commons.lang3.math.NumberUtils.DOUBLE_ZERO;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;


/**
 * Down samples consecutive data points, such as a time-series dataset.
 * 
 * @author harebox
 * @author hyungil.jeong
 */
public class DownSamplers {

	public static final DownSampler MIN = new Min();
	public static final DownSampler MAX = new Max();
	public static final DownSampler AVG = new Avg();

	private DownSamplers() {
	}
	
	static class Min implements DownSampler {

        @Override
        public long sampleLong(Collection<Long> values) {
            if (CollectionUtils.isEmpty(values)) {
                return LONG_ZERO;
            }
            return Collections.min(values);
        }

        @Override
        public double sampleDouble(Collection<Double> values) {
            if (CollectionUtils.isEmpty(values)) {
                return DOUBLE_ZERO;
            }
            return Collections.min(values);
        }

	}

	static class Max implements DownSampler {

        @Override
        public long sampleLong(Collection<Long> values) {
            if (CollectionUtils.isEmpty(values)) {
                return LONG_ZERO;
            }
            return Collections.max(values);
        }

        @Override
        public double sampleDouble(Collection<Double> values) {
            if (CollectionUtils.isEmpty(values)) {
                return DOUBLE_ZERO;
            }
            return Collections.max(values);
        }
	}

	static class Avg implements DownSampler {

        @Override
        public long sampleLong(Collection<Long> values) {
            if (CollectionUtils.isEmpty(values)) {
                return LONG_ZERO;
            }
            long total = 0L;
            for (long value : values) {
                total += value;
            }
            return total / values.size();
        }

        @Override
        public double sampleDouble(Collection<Double> values) {
            if (CollectionUtils.isEmpty(values)) {
                return DOUBLE_ZERO;
            }
            double total = 0D;
            for (double value : values) {
                total += value;
            }
            return total / values.size();
        }
	}

}
