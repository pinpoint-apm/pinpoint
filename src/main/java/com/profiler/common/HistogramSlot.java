package com.profiler.common;

/**
 *
 */
public class HistogramSlot {
    private int slotTime;
    private ResponseCode responseCode;

    public HistogramSlot() {
    }

    public HistogramSlot(int slotTime, ResponseCode responseCode) {
        this.slotTime = slotTime;
        this.responseCode = responseCode;
    }

    public int getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(int slotTime) {
        this.slotTime = slotTime;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

}
