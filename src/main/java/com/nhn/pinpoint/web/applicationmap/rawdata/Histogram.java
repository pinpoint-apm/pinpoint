package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.web.view.HistogramSerializer;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

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
	}

    public void addCallCountByElapsedTime(int elapsedTime) {
        final HistogramSchema schema = serviceType.getHistogramSchema();
        HistogramSlot histogramSlot = schema.findHistogramSlot(elapsedTime);
        short slotTime = histogramSlot.getSlotTime();
        addCallCount(slotTime, 1);
    }

    public Histogram(final short serviceType) {
        this(ServiceType.findServiceType(serviceType));
    }

	// TODO slot번호를 이 클래스에서 추출해야 할 것 같긴 함.
	public void addCallCount(final short slotTime, final long count) {
        final HistogramSchema schema = serviceType.getHistogramSchema();
		if (slotTime == schema.getVerySlowSlot().getSlotTime()) { // 0 is slow slotTime
			this.verySlowCount += count;
            return;
		}
        if (slotTime == schema.getErrorSlot().getSlotTime()) { // -1 is error
			this.errorCount += count;
			return;
		}
        // TODO slotTime 은 <= 아니고 ==으로 수정되어야함.
        if (slotTime <= schema.getFastSlot().getSlotTime()) {
            this.fastCount += count;
            return;
        }
        if (slotTime <= schema.getNormalSlot().getSlotTime()) {
            this.normalCount += count;
            return;
        }
        if (slotTime <= schema.getSlowSlot().getSlotTime()) {
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
		return errorCount + fastCount + normalCount + slowCount + verySlowCount;
	}

    public long getSuccessCount() {
        return fastCount + normalCount + slowCount + verySlowCount;
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
     * User일 경우 예외 상황이 발생할수 있어, schema의 동질여부만 체크하도록 함.
     * Unknown일 경우도 예외 상황있음. Unknown 노드에 HttpClient 호출정보를 머지해야 될경우.
     * @param histogram
     */
    public void addUncheckType(final Histogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        if (serviceType.getHistogramSchema() != histogram.getServiceType().getHistogramSchema()) {
            throw new IllegalArgumentException("schema not equals. this=" + this + ", histogram=" + histogram);

        }
        this.fastCount += histogram.fastCount;
        this.normalCount += histogram.normalCount;
        this.slowCount += histogram.slowCount;
        this.verySlowCount += histogram.verySlowCount;

        this.errorCount += histogram.errorCount;
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
