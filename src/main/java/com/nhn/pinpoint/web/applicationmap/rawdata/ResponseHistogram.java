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

    public ResponseHistogram(final short serviceType) {
        this(ServiceType.findServiceType(serviceType));
    }

	// TODO slot번호를 이 클래스에서 추출해야 할 것 같긴 함.
	public void addSample(final short slotTime, final long count) {
		totalCount += count;
		
		if (slotTime == HistogramSchema.SLOW_SLOT.getSlotTime()) { // 0 is slow slotTime
			slowCount += count;
		} else if (slotTime == HistogramSchema.ERROR_SLOT.getSlotTime()) { // -1 is error
			errorCount += count;
			return;
		}

		final int histogramSlotIndex = histogramSchema.getHistogramSlotIndex(slotTime);
		if (histogramSlotIndex == -1) {
			logger.trace("Can't find slotTime={} count={} serviceType={}", slotTime, count, serviceType);
			return;
		}
		values[histogramSlotIndex] += count;
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

	public void add(ResponseHistogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        if (this.serviceType != histogram.serviceType) {
            throw new IllegalArgumentException("this=" + this + ", histogram=" + histogram);
        }
        addValues(histogram);

		this.totalCount += histogram.totalCount;
		this.errorCount += histogram.errorCount;
		this.slowCount += histogram.slowCount;
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
        // TODO MAP에서 사용하지 않는다면 제거할것. equals를 직접 선언해서 사용하기 애매함.
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
