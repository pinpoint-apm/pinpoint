package com.nhn.pinpoint.common;

/**
 * @author emeroad
 */
public class HistogramSlot {
    private final short slotTime;
    private final ResponseCode responseCode;


    public HistogramSlot(short slotTime, ResponseCode responseCode) {
        if (responseCode == null) {
            throw new NullPointerException("responseCode must not be null");
        }
        this.slotTime = slotTime;
        this.responseCode = responseCode;
    }

    // TODO : int를 short으로 바꿔야할 듯...
    public short getSlotTime() {
        return slotTime;
    }


    public ResponseCode getResponseCode() {
        return responseCode;
    }

}
