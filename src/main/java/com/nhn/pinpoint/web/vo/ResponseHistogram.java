package com.nhn.pinpoint.web.vo;

import java.util.List;

import com.nhn.pinpoint.common.Histogram;
import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class ResponseHistogram {

	private final ServiceType serviceType;
    private final Histogram histogram;
    private final long[] values;

	private long errorCount;
	private long slowCount;

	public ResponseHistogram(ServiceType serviceType) {
		this.serviceType = serviceType;
        this.histogram = serviceType.getHistogram();
        // TODO value에 저장하는 구조 추가 수정 필요.
        int size = histogram.getHistogramSlotList().size();
		values = new long[size ];

	}

	public void addSample(short slot, long value) {
		if (slot == 0) { // 0 is slow slot
			slowCount += value;
		}

        int histogramSlotIndex = histogram.getHistogramSlotIndex(slot);
        if (histogramSlotIndex == -1) {
            return;
        }
        values[histogramSlotIndex] += value;
	}

	public void addSample(long elapsed) {

        int histogramSlotIndex = histogram.findHistogramSlotIndex((int) elapsed);
        if (histogramSlotIndex == -1) {
            slowCount++;
            return;
        }
        values[histogramSlotIndex]++;
	}

	public void incrErrorCount(long value) {
		errorCount += value;
	}

	public ServiceType getServiceType() {
		return serviceType;
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

	public void mergeWith(ResponseHistogram histogram) {
		if (!this.equals(histogram)) {
			throw new IllegalArgumentException();
		}

		long[] otherValues = histogram.values;
		for (int i = 0; i < values.length; i++) {
			values[i] += otherValues[i];
		}

		this.errorCount += histogram.errorCount;
		this.slowCount += histogram.slowCount;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((histogram == null) ? 0 : histogram.hashCode());
		result = prime * result + ((serviceType == null) ? 0 : serviceType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResponseHistogram other = (ResponseHistogram) obj;
		if (histogram == null) {
			if (other.histogram != null)
				return false;
		} else if (!histogram.equals(other.histogram))
			return false;
		if (serviceType != other.serviceType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
        List<HistogramSlot> histogramSlotList = histogram.getHistogramSlotList();
        for (int i = 0; i < histogramSlotList.size(); i++) {
            HistogramSlot histogramSlot = histogramSlotList.get(i);
            sb.append('"').append(histogramSlot.getSlotTime()).append('"').append(" : ").append(values[i]);
			if (i < histogramSlotList.size() - 1) {
				sb.append(", ");
			}
		}

		sb.append(" }");

		return sb.toString();
	}
}
