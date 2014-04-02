package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.web.view.HistogramSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;


/**
 *
 * @author emeroad
 * @author netspider
 */
@JsonSerialize(using=HistogramSerializer.class)
public class Histogram {
	
	private final HistogramSchema schema;

    private long fastCount;
    private long normalCount;
    private long slowCount;
    private long verySlowCount;

	private long errorCount;


    public Histogram(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
		this.schema = serviceType.getHistogramSchema();
	}

    public Histogram(HistogramSchema schema) {
        if (schema == null) {
            throw new NullPointerException("schema must not be null");
        }
        this.schema = schema;
    }


    public void addCallCountByElapsedTime(int elapsedTime) {
        final HistogramSlot histogramSlot = this.schema.findHistogramSlot(elapsedTime);
        short slotTime = histogramSlot.getSlotTime();
        addCallCount(slotTime, 1);
    }

    public Histogram(final short serviceType) {
        this(ServiceType.findServiceType(serviceType));
    }

	// TODO slot번호를 이 클래스에서 추출해야 할 것 같긴 함.
	public void addCallCount(final short slotTime, final long count) {
        final HistogramSchema schema = this.schema;
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

	public HistogramSchema getHistogramSchema() {
		return this.schema;
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

	public void add(final Histogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        if (this.schema != histogram.schema) {
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

        if (schema != histogram.schema) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return schema.hashCode();
    }

    @Override
    public String toString() {
        return "Histogram{" +
                "schema=" + schema +
                ", fastCount=" + fastCount +
                ", normalCount=" + normalCount +
                ", slowCount=" + slowCount +
                ", verySlowCount=" + verySlowCount +
                ", errorCount=" + errorCount +
                '}';
    }

}
