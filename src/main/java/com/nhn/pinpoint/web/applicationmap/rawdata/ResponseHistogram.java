package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.Histogram;
import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.util.JsonSerializable;
import com.nhn.pinpoint.web.util.Mergeable;

/**
 * 
 * @author netspider
 * 
 */
public class ResponseHistogram implements JsonSerializable {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final ServiceType serviceType;
	private final Histogram histogram;
	private final long[] values;

	private long totalCount;
	private long errorCount;
	private long slowCount;

	public ResponseHistogram(ServiceType serviceType) {
//		this.id = id;
		this.serviceType = serviceType;
		this.histogram = serviceType.getHistogram();
		// TODO value에 저장하는 구조 추가 수정 필요.
		int size = histogram.getHistogramSlotList().size();
		values = new long[size];
	}

	// TODO slot번호를 이 클래스에서 추출해야 할 것 같긴 함.
	public void addSample(short slot, long value) {
		totalCount += value;
		
		if (slot == 0) { // 0 is slow slot
			slowCount += value;
		} else if (slot == -1) { // -1 is error
			errorCount += value;
			return;
		}

		int histogramSlotIndex = histogram.getHistogramSlotIndex(slot);
		if (histogramSlotIndex == -1) {
			logger.debug("Can't find slot={} value={} serviceType={}", slot, value, serviceType);
			return;
		}
		values[histogramSlotIndex] += value;
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
		return totalCount;
	}

	public ResponseHistogram add(ResponseHistogram histogram) {
		if (!this.equals(histogram)) {
			throw new IllegalArgumentException("A=" + this + ", B=" + histogram);
		}

		long[] otherValues = histogram.values;
		for (int i = 0; i < values.length; i++) {
			values[i] += otherValues[i];
		}

		this.totalCount += histogram.totalCount;
		this.errorCount += histogram.errorCount;
		this.slowCount += histogram.slowCount;
		
		return this;
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
		// if (serviceType != other.serviceType) {
		// return false;
		// }
		return true;
	}
	 
	@Override
	public String toString() {
		return "ResponseHistogram [serviceType=" + serviceType + ",json=" + getJson() + "]";
	}


	@Override
	public String getJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		List<HistogramSlot> histogramSlotList = histogram.getHistogramSlotList();
		HistogramSlot histogramSlot = null;
		for (int i = 0; i < histogramSlotList.size(); i++) {
			histogramSlot = histogramSlotList.get(i);
			sb.append('"').append(histogramSlot.getSlotTime()).append('"').append(":").append(values[i]);
			if (i < histogramSlotList.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append(",\"").append(histogramSlot.getSlotTime()).append("+\"").append(":").append(slowCount);
		sb.append(",\"error\":").append(errorCount);
		sb.append(" }");

		return sb.toString();
	}
}
