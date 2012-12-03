package com.profiler.common.bo;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * 
 * @author netspider
 * 
 */
public class HistogramBo implements Serializable {

	private static final long serialVersionUID = 4613517226343565010L;

	public static final int DEFAULT_RESOLUTION_MS = 100;

	private final TreeMap<Integer, Integer> counts = new TreeMap<Integer, Integer>();
	private final int resolution;

	private int min = 0;
	private int max = 0;
	private int sampleCount = 1;

	public HistogramBo(int resolution) {
		this.resolution = resolution;
	}

	public void addSample(int value) {
		if (min > value) {
			min = value;
		}

		if (max < value) {
			max = value;
		}

		int slot = value / resolution;
		Integer org = counts.get(slot);

		if (org == null) {
			counts.put(slot, 1);
		} else {
			counts.put(slot, ++org);
		}

		sampleCount++;
	}

	public HistogramBo mergeWith(HistogramBo histogram) {
		if (this.resolution != histogram.resolution) {
			throw new IllegalArgumentException("Can't merge Histogram. different resolution.");
		}

		if (this.min > histogram.min) {
			this.min = histogram.min;
		}

		if (this.max < histogram.max) {
			this.max = histogram.max;
		}

		sampleCount += histogram.sampleCount;

		for (Entry<Integer, Integer> entry : histogram.counts.entrySet()) {
			int slot = entry.getKey();
			Integer org = counts.get(slot);

			if (org == null) {
				counts.put(slot, entry.getValue());
			} else {
				counts.put(slot, org + entry.getValue());
			}
		}

		return this;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public int getSampleCount() {
		return sampleCount;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Iterator<Entry<Integer, Integer>> iterator = counts.entrySet().iterator();

		sb.append("[");
		while (iterator.hasNext()) {
			Entry<Integer, Integer> entry = iterator.next();
			sb.append("{ \"from\" : ").append(entry.getKey() * resolution).append(", ");
			sb.append("\"to\" : ").append(entry.getKey() * resolution + resolution).append(", ");
			sb.append("\"value\" : ").append(entry.getValue()).append(" }");
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");

		return sb.toString();
	}
}
