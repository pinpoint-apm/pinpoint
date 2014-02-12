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

    private long totalCount;

    private long fastCount;
    private long normalCount;
    private long slowCount;
    private long verySlowCount;

	private long errorCount;


	public ResponseHistogram(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
		this.serviceType = serviceType;
		this.histogramSchema = serviceType.getHistogramSchema();
	}

    public void addElapsedTime(int elapsedTime) {
        HistogramSlot histogramSlot = histogramSchema.findHistogramSlot(elapsedTime);
        short slotTime = histogramSlot.getSlotTime();
        addSample(slotTime, 1);
    }

    public ResponseHistogram(final short serviceType) {
        this(ServiceType.findServiceType(serviceType));
    }

	// TODO slot번호를 이 클래스에서 추출해야 할 것 같긴 함.
	public void addSample(final short slotTime, final long count) {
		this.totalCount += count;
		
		if (slotTime == histogramSchema.getVerySlowSlot().getSlotTime()) { // 0 is slow slotTime
			this.verySlowCount += count;
            return;
		}
        if (slotTime == histogramSchema.getErrorSlot().getSlotTime()) { // -1 is error
			this.errorCount += count;
			return;
		}
        // TODO slotTime 은 <= 아니고 ==으로 수정되어야함.
        if (slotTime <= histogramSchema.getFastSlot().getSlotTime()) {
            this.fastCount += count;
            return;
        }
        if (slotTime <= histogramSchema.getNormalSlot().getSlotTime()) {
            this.normalCount += count;
            return;
        }
        if (slotTime <= histogramSchema.getSlowSlot().getSlotTime()) {
            this.slowCount += count;
            return;
        }
        throw new IllegalArgumentException("slot not found slotTime:" + slotTime + " count:" + count);
	}

	public ServiceType getServiceType() {
		return serviceType;
	}


	public long getErrorCount() {
		return errorCount;
	}

    public long getFastCount() {
        return fastCount;
    }

    public long getNormalCount() {
        return normalCount;
    }

    public long getSlowCount() {
        return slowCount;
    }

    public long getVerySlowCount() {
		return verySlowCount;
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
        addUncheckType(histogram);
	}

    /**
     * 같은 타입인지 체크하지 않음.
     * @param histogram
     */
    public void addUncheckType(final ResponseHistogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        this.fastCount += histogram.fastCount;
        this.normalCount += histogram.normalCount;
        this.slowCount += histogram.slowCount;
        this.verySlowCount += histogram.verySlowCount;

        this.errorCount += histogram.errorCount;

        this.totalCount += histogram.totalCount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResponseHistogram histogram = (ResponseHistogram) o;

        if (serviceType != histogram.serviceType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return serviceType.hashCode();
    }

    @Override
    public String toString() {
        return "ResponseHistogram{" +
                "serviceType=" + serviceType +
                ", histogramSchema=" + histogramSchema +
                ", totalCount=" + totalCount +
                ", fastCount=" + fastCount +
                ", normalCount=" + normalCount +
                ", slowCount=" + slowCount +
                ", verySlowCount=" + verySlowCount +
                ", errorCount=" + errorCount +
                '}';
    }

    @Override
	public String getJson() {
		final StringBuilder sb = new StringBuilder(128);
		sb.append("{ ");

        appendSlotTimeAndCount(sb, histogramSchema.getFastSlot().getSlotTime(), fastCount);
        sb.append(", ");
        appendSlotTimeAndCount(sb, histogramSchema.getNormalSlot().getSlotTime(), normalCount);
        sb.append(", ");
        appendSlotTimeAndCount(sb, histogramSchema.getSlowSlot().getSlotTime(), slowCount);
        sb.append(", ");
        // very slow는 0값이라 slow 값을 사용해야 한다.
        appendSlotTimeAndCount(sb, histogramSchema.getSlowSlot().getSlotTime() + "+", verySlowCount);
        sb.append(", ");
        appendSlotTimeAndCount(sb, "error", errorCount);
        sb.append(" }");

		return sb.toString();
	}

    private void appendSlotTimeAndCount(StringBuilder sb, short slotTime, long count) {
        appendSlotTimeAndCount(sb, Short.toString(slotTime), count);
    }

    private void appendSlotTimeAndCount(StringBuilder sb, String slotTimeName, long count) {
        sb.append('"');
        sb.append(slotTimeName);
        sb.append('"');
        sb.append(":");
        sb.append(count);
    }
}
