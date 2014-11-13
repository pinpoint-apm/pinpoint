package com.nhn.pinpoint.web.vo.linechart;

import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;
import static org.apache.commons.lang3.math.NumberUtils.DOUBLE_ZERO;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;


/**
 * Time-series 처럼 연속된 데이터를 다운 샘플링한다.
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
