package com.nhn.pinpoint.common;

/**
 * @author emeroad
 */
public class HistogramSlot {
    private final short slotTime;
    private final SlotType slotType;


    public HistogramSlot(short slotTime, SlotType slotType) {
        if (slotType == null) {
            throw new NullPointerException("slotType must not be null");
        }
        this.slotTime = slotTime;
        this.slotType = slotType;
    }

    public short getSlotTime() {
        return slotTime;
    }


    public SlotType getSlotType() {
        return slotType;
    }

    @Override
    public String toString() {
        return "HistogramSlot{" +
                "slotTime=" + slotTime +
                ", slotType=" + slotType +
                '}';
    }
}
