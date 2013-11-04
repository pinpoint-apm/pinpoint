package com.nhn.pinpoint.common;

/**
 * @author emeroad
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

    // TODO : int를 short으로 바꿔야할 듯...
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
