package com.nhn.pinpoint.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class Histogram {

    public static final HistogramSlot SLOW_SLOT = new HistogramSlot(0, ResponseCode.NORMAL);
    public static final HistogramSlot ERROR_SLOT = new HistogramSlot(-1, ResponseCode.NORMAL);
    
    public static final Histogram FAST;
    public static final Histogram NORMAL;

    static {
        FAST = new Histogram(1);
        FAST.addHistogramSlot(new HistogramSlot(100, ResponseCode.NORMAL));
        FAST.addHistogramSlot(new HistogramSlot(300, ResponseCode.NORMAL));
        FAST.addHistogramSlot(new HistogramSlot(500, ResponseCode.WARN));

        NORMAL = new Histogram(2);
        NORMAL.addHistogramSlot(new HistogramSlot(1000, ResponseCode.NORMAL));
        NORMAL.addHistogramSlot(new HistogramSlot(3000, ResponseCode.NORMAL));
        NORMAL.addHistogramSlot(new HistogramSlot(5000, ResponseCode.WARN));
    }
    // ** histogramSlot list는 항상 정렬된 list 이어야 한다.
    // 지금은 그냥 사람이 한다.
    private final List<HistogramSlot> histogramSlotList = new ArrayList<HistogramSlot>(3);
    private final int typeCode;

    // 내부에서 생성한 FAST, NORMAL등의 참조만 사용할것
    private Histogram(int typeCode) {
    	this.typeCode = typeCode;
    }

    public void addHistogramSlot(HistogramSlot slot) {
        this.histogramSlotList.add(slot);
    }

    public List<HistogramSlot> getHistogramSlotList() {
        return histogramSlotList;
    }

    /**
     * elapsed 기준으로 가장 적합한 슬롯을 찾는다.
     * @param elapsed
     * @return
     */
    public HistogramSlot findHistogramSlot(int elapsed) {
        for (HistogramSlot slot : histogramSlotList) {
            if (elapsed < slot.getSlotTime()) {
                return slot;
            }
        }
        return SLOW_SLOT;
    }

    /**
     * elapsed 기준으로 가장 적합한 슬롯을 찾는다.
     * @param elapsed
     * @return
     */
    public int findHistogramSlotIndex(int elapsed) {
        final int size = histogramSlotList.size();
        for(int i = 0; i < size; i++) {
            HistogramSlot slot = histogramSlotList.get(i);
            if (elapsed <= slot.getSlotTime()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 정확한 slotNumber를 기준으로 slot의 index 번호를 찾는다.
     * @param slotNumber
     * @return
     */
    public int getHistogramSlotIndex(int slotNumber) {
        final int size = histogramSlotList.size();
        for(int i = 0; i < size; i++) {
            HistogramSlot slot = histogramSlotList.get(i);
            if (slotNumber == slot.getSlotTime()) {
                return i;
            }
        }
        return -1;
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
		Histogram other = (Histogram) obj;
		if (typeCode != other.typeCode)
			return false;
		return true;
	}
}
