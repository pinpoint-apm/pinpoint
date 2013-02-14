package com.nhn.hippo.web.vo;

import java.util.Arrays;
import java.util.List;

import com.profiler.common.Histogram;
import com.profiler.common.HistogramSlot;
import com.profiler.common.ServiceType;

/**
 * 
 * @author netspider
 * 
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

        int histogramSlotIndex = histogram.getHistogramSlotIndex((int) elapsed);
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
