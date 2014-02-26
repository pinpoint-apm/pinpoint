package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.web.view.HistogramSerializer;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;

import java.io.IOException;

/**
 * 
 * @author netspider
 * 
 */
@JsonSerialize(using=HistogramSerializer.class)
public class Histogram {
	
	private final ServiceType serviceType;
	private final HistogramSchema histogramSchema;

    private long totalCount;

    private long fastCount;
    private long normalCount;
    private long slowCount;
    private long verySlowCount;

	private long errorCount;


    public Histogram(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
		this.serviceType = serviceType;
		this.histogramSchema = serviceType.getHistogramSchema();
	}

    public void addCallCountByElapsedTime(int elapsedTime) {
        HistogramSlot histogramSlot = histogramSchema.findHistogramSlot(elapsedTime);
        short slotTime = histogramSlot.getSlotTime();
        addCallCount(slotTime, 1);
    }

    public Histogram(final short serviceType) {
        this(ServiceType.findServiceType(serviceType));
    }

	// TODO slot번호를 이 클래스에서 추출해야 할 것 같긴 함.
	public void addCallCount(final short slotTime, final long count) {
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

    public long getCount(SlotType slotType) {
        if (slotType == null) {
            throw new NullPointerException("slotType must not be null");
        }

        switch (slotType) {
            case FAST:
                return fastCount;
            case NORMAL:
                return normalCount;
            case SLOW:
                return slowCount;
            case VERY_SLOW:
                return verySlowCount;
            case ERROR:
                return errorCount;
        }
        throw new IllegalArgumentException("slotType:" + slotType);
    }

	public void add(Histogram histogram) {
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
    public void addUncheckType(final Histogram histogram) {
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

        Histogram histogram = (Histogram) o;

        if (serviceType != histogram.serviceType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return serviceType.hashCode();
    }

    @Override
    public String toString() {
        return "Histogram{" +
                "serviceType=" + serviceType +
                ", totalCount=" + totalCount +
                ", fastCount=" + fastCount +
                ", normalCount=" + normalCount +
                ", slowCount=" + slowCount +
                ", verySlowCount=" + verySlowCount +
                ", errorCount=" + errorCount +
                '}';
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();


	public String getJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

	}


}
