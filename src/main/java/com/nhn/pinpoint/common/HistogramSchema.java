package com.nhn.pinpoint.common;

/**
 * @author emeroad
 */
public class HistogramSchema {

    public static final short VERY_SLOW_SLOT_TIME = 0;

    public static final short ERROR_SLOT_TIME = -1;

    public static final HistogramSchema FAST_SCHEMA;
    public static final HistogramSchema NORMAL_SCHEMA;

    static {
        FAST_SCHEMA = new HistogramSchema(1, (short)100, "100ms", (short)300, "300ms", (short)500, "500ms", "Slow", "Error");

        NORMAL_SCHEMA = new HistogramSchema(2, (short)1000, "1.0s",  (short)3000, "3.0s", (short)5000, "5.0s", "Slow", "Error");
    }
    // ** histogramSlot list는 항상 정렬된 list 이어야 한다.
    // 지금은 그냥 사람이 한다.
//    private final List<HistogramSlot> histogramSlotList = new ArrayList<HistogramSlot>(3);
    private final int typeCode;

    private final HistogramSlot fastSlot;
    private final HistogramSlot normalSlot;
    private final HistogramSlot slowSlot;
    private final HistogramSlot verySlowSlot;
    private final HistogramSlot errorSlot;

    // 내부에서 생성한 FAST_SCHEMA, NORMAL등의 참조만 사용할것
    private HistogramSchema(int typeCode, short fast, String fastName, short normal, String normalName, short slow, String slowName, String verySlowName, String errorName) {
    	this.typeCode = typeCode;
        this.fastSlot = new HistogramSlot(fast, SlotType.FAST, fastName);
        this.normalSlot = new HistogramSlot(normal, SlotType.NORMAL, normalName);
        this.slowSlot = new HistogramSlot(slow, SlotType.SLOW, slowName);
        this.verySlowSlot = new HistogramSlot(VERY_SLOW_SLOT_TIME, SlotType.VERY_SLOW, verySlowName);
        this.errorSlot = new HistogramSlot(ERROR_SLOT_TIME, SlotType.ERROR, errorName);
    }

    public int getTypeCode() {
        return typeCode;
    }

    /**
     * elapsedTime 기준으로 가장 적합한 슬롯을 찾는다.
     * @param elapsedTime
     * @return
     */
    public HistogramSlot findHistogramSlot(int elapsedTime) {
        if (elapsedTime == ERROR_SLOT_TIME) {
            return errorSlot;
        }
        if (elapsedTime <= this.fastSlot.getSlotTime()) {
            return fastSlot;
        }
        if (elapsedTime <= this.normalSlot.getSlotTime()) {
            return normalSlot;
        }
        if (elapsedTime <= this.slowSlot.getSlotTime()) {
            return slowSlot;
        }
        return verySlowSlot;
    }

    public HistogramSlot getHistogramSlot(final short slotTime) {
        if (slotTime == ERROR_SLOT_TIME) {
            return errorSlot;
        }
        if (slotTime == this.fastSlot.getSlotTime()) {
            return fastSlot;
        }
        if (slotTime == this.normalSlot.getSlotTime()) {
            return normalSlot;
        }
        if (slotTime == this.slowSlot.getSlotTime()) {
            return slowSlot;
        }
        if (slotTime == this.verySlowSlot.getSlotTime()) {
            return slowSlot;
        }
        throw new IllegalArgumentException("HistogramSlot not found. slotTime:" + slotTime);
    }

    public HistogramSlot getFastSlot() {
        return fastSlot;
    }

    public HistogramSlot getNormalSlot() {
        return normalSlot;
    }

    public HistogramSlot getSlowSlot() {
        return slowSlot;
    }

    public HistogramSlot getVerySlowSlot() {
        return verySlowSlot;
    }

    public HistogramSlot getErrorSlot() {
        return errorSlot;
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
		if (typeCode != other.typeCode)
			return false;
		return true;
	}

    @Override
    public String toString() {
        return "HistogramSchema{" +
                "typeCode=" + typeCode +
                ", fastSlot=" + fastSlot +
                ", normalSlot=" + normalSlot +
                ", slowSlot=" + slowSlot +
                ", verySlowSlot=" + verySlowSlot +
                ", errorSlot=" + errorSlot +
                '}';
    }
}
