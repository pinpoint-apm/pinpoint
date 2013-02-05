package com.nhn.hippo.web.vo;

import java.util.Arrays;

import com.profiler.common.ServiceType;

/**
 * 
 * @author netspider
 * 
 */
public class ResponseHistogram {

	private final ServiceType serviceType;
	private final short[] slots;
	private final long[] values;

	private long errorCount;
	private long slowCount;

	public ResponseHistogram(ServiceType serviceType) {
		this.serviceType = serviceType;

		short[] srcSlots = serviceType.getHistogramSlots();
		slots = Arrays.copyOf(srcSlots, srcSlots.length);

		values = new long[slots.length];
		Arrays.fill(values, 0L);
	}

	public void addSample(short slot, long value) {
		if (slot == 0) { // 0 is slow slot
			slowCount += value;
		}
		for (int i = 0; i < slots.length; i++) {
			if (slots[i] == slot) {
				values[i] += value;
				return;
			}
		}
	}

	public void addSample(long elapsed) {
		for (int i = 0; i < slots.length; i++) {
			if (elapsed < slots[i]) {
				values[i]++;
				return;
			}
		}
		slowCount++;
	}

	public void incrErrorCount(long value) {
		errorCount += value;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public short[] getSlots() {
		return slots;
	}

	public long[] getValues() {
		return values;
	}

	public long getErrorCount() {
		return errorCount;
	}

	public long getSlowCount() {
		return slowCount;
	}

	public long getTotalCount() {
		long total = errorCount + slowCount;
		for (long v : values) {
			total += v;
		}
		return total;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");

		for (int i = 0; i < slots.length; i++) {
			sb.append('"').append(slots[i]).append('"').append(" : ").append(values[i]);
			if (i < slots.length - 1) {
				sb.append(", ");
			}
		}

		sb.append(" }");

		return sb.toString();
	}
}
