package com.nhn.pinpoint.web.vo.linechart;


/**
 * Time-series 처럼 연속된 데이터를 다운 샘플링한다.
 * 
 * @author harebox
 */
public class DownSamplers {

	public static final DownSampler MIN = new Min();
	public static final DownSampler MAX = new Max();
	public static final DownSampler AVG = new Avg();

	private DownSamplers() {
	}
	
	static class Min implements DownSampler {

		public long sampleLong(Long[] longs) {
			if (longs == null || longs.length == 0) {
				return 0;
			}
			Long min = Long.MAX_VALUE;
			for (Long each : longs) {
				if (min > each) {
					min = each;
				}
			}
			return min;
		}

		public double sampleDouble(Double[] doubles) {
			if (doubles == null || doubles.length == 0) {
				return 0.0;
			}
			Double min = Double.MAX_VALUE;
			for (Double each : doubles) {
				if (min > each) {
					min = each;
				}
			}
			return min;
		}

	}

	static class Max implements DownSampler {

		public long sampleLong(Long[] longs) {
			if (longs == null || longs.length == 0) {
				return 0;
			}
			Long max = Long.MIN_VALUE;
			for (Long each : longs) {
				if (max < each) {
					max = each;
				}
			}
			return max;
		}

		public double sampleDouble(Double[] doubles) {
			if (doubles == null || doubles.length == 0) {
				return 0.0;
			}
			Double max = Double.MIN_VALUE;
			for (Double each : doubles) {
				if (max < each) {
					max = each;
				}
			}
			return max;
		}

	}

	static class Avg implements DownSampler {
		
		public long sampleLong(Long[] longs) {
			if (longs == null || longs.length == 0) {
				return 0;
			}
			long total = 0;
			for (Long each : longs) {
				total += each;
			}
			return total / longs.length;
		}

		public double sampleDouble(Double[] doubles) {
			if (doubles == null || doubles.length == 0) {
				return 0.0;
			}
			double total = 0.0;
			for (Double each : doubles) {
				total += each;
			}
			return total / doubles.length;
		}

	}

}
