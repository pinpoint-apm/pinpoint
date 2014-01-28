package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.List;

import com.nhn.pinpoint.common.HistogramSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.util.JsonSerializable;

/**
 * 
 * @author netspider
 * 
 */
public class ResponseHistogram implements JsonSerializable {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final ServiceType serviceType;
	private final HistogramSchema histogramSchema;
	private final long[] values;

	private long totalCount;
	private long errorCount;
	private long slowCount;

	public ResponseHistogram(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
		this.serviceType = serviceType;
		this.histogramSchema = serviceType.getHistogramSchema();
		// TODO value에 저장하는 구조 추가 수정 필요.
		this.values = histogramSchema.createNode();
	}

    public ResponseHistogram(short serviceType) {
        this(ServiceType.findServiceType(serviceType));
    }

	// TODO slot번호를 이 클래스에서 추출해야 할 것 같긴 함.
	public void addSample(short slot, long value) {
		totalCount += value;
		
		if (slot == HistogramSchema.SLOW_SLOT.getSlotTime()) { // 0 is slow slot
			slowCount += value;
		} else if (slot == HistogramSchema.ERROR_SLOT.getSlotTime()) { // -1 is error
			errorCount += value;
			return;
		}

		final int histogramSlotIndex = histogramSchema.getHistogramSlotIndex(slot);
		if (histogramSlotIndex == -1) {
			logger.trace("Can't find slot={} value={} serviceType={}", slot, value, serviceType);
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

        addValues(histogram);

		this.totalCount += histogram.totalCount;
		this.errorCount += histogram.errorCount;
		this.slowCount += histogram.slowCount;
		
		return this;
	}

    private void addValues(ResponseHistogram histogram) {
        final long[] otherValues = histogram.values;
        final int length = values.length;
        for (int i = 0; i < length; i++) {
			this.values[i] += otherValues[i];
		}
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
		final StringBuilder sb = new StringBuilder(128);
		sb.append("{ ");
		List<HistogramSlot> histogramSlotList = histogramSchema.getHistogramSlotList();
		HistogramSlot histogramSlot = null;
		for (int i = 0; i < histogramSlotList.size(); i++) {
			histogramSlot = histogramSlotList.get(i);
			sb.append('"');
            sb.append(histogramSlot.getSlotTime());
            sb.append('"');
            sb.append(":");
            sb.append(values[i]);
			if (i < histogramSlotList.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append(",\"");
        if (histogramSlot == null) {
            // 이상한 상태값이므로 일단 명시적으로 에러가 발생하도록 수정한다.
            throw new IllegalStateException("histogramSlot is null");
        }
        sb.append(histogramSlot.getSlotTime());
        sb.append("+\"");
        sb.append(":");
        sb.append(slowCount);
		sb.append(",\"error\":");
        sb.append(errorCount);
		sb.append(" }");

		return sb.toString();
	}
}
