package com.navercorp.pinpoint.common.trace;

/**
 * Created by Naver on 2015-11-05.
 */
public class BaseHistogramSchema implements HistogramSchema {
    public static  HistogramSchema FAST_SCHEMA = new BaseHistogramSchema(1, (short) 100, "100ms", (short) 101, "100ms error", (short) 300, "300ms", (short) 301, "300ms error", (short) 500, "500ms", (short) 501, "500ms error", (short) 0, "Slow", (short) -1, "Slow error");
    public static HistogramSchema NORMAL_SCHEMA = new BaseHistogramSchema(2, (short) 1000, "1s", (short) 1001, "1s error", (short) 3000, "3s", (short) 3001, "3s error", (short) 5000, "5s", (short) 5001, "5s error", (short) 0, "Slow", (short) -1, "Slow error");

    private final int typeCode;

    private final HistogramSlot fastSlot;
    private final HistogramSlot fastErrorSlot;
    private final HistogramSlot normalSlot;
    private final HistogramSlot normalErrorSlot;
    private final HistogramSlot slowSlot;
    private final HistogramSlot slowErrorSlot;
    private final HistogramSlot verySlowSlot;
    private final HistogramSlot verySlowErrorSlot;

    // Should use the reference of FAST_SCHEMA, NORMAL created internally
    private BaseHistogramSchema(int typeCode, short fast, String fastName, short fastError, String fastErrorName, short normal, String normalName, short normalError, String normalErrorName, short slow, String slowName, short slowError, String slowErrorName, short verySlow, String verySlowName, short verySlowError, String verySlowErrorName) {
        this.typeCode = typeCode;
        this.fastSlot = new HistogramSlot(fast, SlotType.FAST, fastName);
        this.fastErrorSlot = new HistogramSlot(fastError, SlotType.FAST_ERROR, fastErrorName);
        this.normalSlot = new HistogramSlot(normal, SlotType.NORMAL, normalName);
        this.normalErrorSlot = new HistogramSlot(normalError, SlotType.NORMAL_ERROR, normalErrorName);
        this.slowSlot = new HistogramSlot(slow, SlotType.SLOW, slowName);
        this.slowErrorSlot = new HistogramSlot(slowError, SlotType.SLOW_ERROR, slowErrorName);
        this.verySlowSlot = new HistogramSlot(verySlow, SlotType.VERY_SLOW, verySlowName);
        this.verySlowErrorSlot = new HistogramSlot(verySlowError, SlotType.VERY_SLOW_ERROR, verySlowErrorName);
    }

    public int getTypeCode() {
        return typeCode;
    }

    /**
     * find the most appropriate slot based on elapsedTime
     *
     * @param elapsedTime
     * @return
     */
    public HistogramSlot findHistogramSlot(int elapsedTime) {
        return findHistogramSlot(elapsedTime, false);
    }

    public HistogramSlot findHistogramSlot(int elapsedTime, boolean error) {
        if (elapsedTime <= this.fastSlot.getSlotTime()) {
            return error ? fastErrorSlot : fastSlot;
        }

        if (elapsedTime <= this.normalSlot.getSlotTime()) {
            return error ? normalErrorSlot : normalSlot;
        }
        if (elapsedTime <= this.slowSlot.getSlotTime()) {
            return error ? slowErrorSlot : slowSlot;
        }
        return error ? verySlowErrorSlot : verySlowSlot;
    }

    public HistogramSlot getHistogramSlot(final short slotTime) {
        if (slotTime == this.fastSlot.getSlotTime()) {
            return fastSlot;
        }
        if (slotTime == this.fastErrorSlot.getSlotTime()) {
            return fastErrorSlot;
        }

        if (slotTime == this.normalSlot.getSlotTime()) {
            return normalSlot;
        }
        if (slotTime == this.normalErrorSlot.getSlotTime()) {
            return normalErrorSlot;
        }

        if (slotTime == this.slowSlot.getSlotTime()) {
            return slowSlot;
        }
        if (slotTime == this.slowErrorSlot.getSlotTime()) {
            return slowErrorSlot;
        }

        if (slotTime == this.verySlowSlot.getSlotTime()) {
            return slowSlot;
        }
        if (slotTime == this.verySlowErrorSlot.getSlotTime()) {
            return slowErrorSlot;
        }

        throw new IllegalArgumentException("HistogramSlot not found. slotTime:" + slotTime);
    }

    @Override
    public HistogramSlot getFastSlot() {
        return fastSlot;
    }

    @Override
    public HistogramSlot getNormalSlot() {
        return normalSlot;
    }

    @Override
    public HistogramSlot getSlowSlot() {
        return slowSlot;
    }

    @Override
    public HistogramSlot getVerySlowSlot() {
        return verySlowSlot;
    }

    @Override
    public HistogramSlot getFastErrorSlot() {
        return fastErrorSlot;
    }

    @Override
    public HistogramSlot getNormalErrorSlot() {
        return normalErrorSlot;
    }

    @Override
    public HistogramSlot getSlowErrorSlot() {
        return slowErrorSlot;
    }

    @Override
    public HistogramSlot getVerySlowErrorSlot() {
        return verySlowErrorSlot;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + typeCode;
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
        HistogramSchema other = (HistogramSchema) obj;
        if (typeCode != other.getTypeCode())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "{" +
                "typeCode=" + typeCode +
                ", fastSlot=" + fastSlot +
                ", fastErrorSlot=" + fastErrorSlot +
                ", normalSlot=" + normalSlot +
                ", normalErrorSlot=" + normalErrorSlot +
                ", slowSlot=" + slowSlot +
                ", slowErrorSlot=" + slowErrorSlot +
                ", verySlowSlot=" + verySlowSlot +
                ", verySlowErrorSlot=" + verySlowErrorSlot +
                '}';
    }
}
