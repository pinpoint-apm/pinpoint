package com.nhn.pinpoint.web.vo;

import java.util.List;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 * @author emeroad
 */
@Deprecated
public class ResponseHistogram {

	private final ServiceType serviceType;
    private final HistogramSchema histogramSchema;
    private final long[] values;

	private long errorCount;
	private long slowCount;

	public ResponseHistogram(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.serviceType = serviceType;
        this.histogramSchema = serviceType.getHistogramSchema();
        // TODO value에 저장하는 구조 추가 수정 필요.
        this.values =  this.histogramSchema.createNode();
	}

	public void addSample(short slot, long count) {
		if (slot == 0) { // 0 is slow slot
			slowCount += count;
		}

        int histogramSlotIndex = histogramSchema.getHistogramSlotIndex(slot);
        if (histogramSlotIndex == -1) {
            return;
        }
        values[histogramSlotIndex] += count;
	}

	public void addSample(long elapsed) {

        int histogramSlotIndex = histogramSchema.findHistogramSlotIndex((int) elapsed);
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

    @Deprecated
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
		result = prime * result + ((histogramSchema == null) ? 0 : histogramSchema.hashCode());
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
		if (histogramSchema == null) {
			if (other.histogramSchema != null)
				return false;
		} else if (!histogramSchema.equals(other.histogramSchema))
			return false;
		if (serviceType != other.serviceType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
        List<HistogramSlot> histogramSlotList = histogramSchema.getHistogramSlotList();
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
