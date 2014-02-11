package com.nhn.pinpoint.common;

/**
 * @author emeroad
 */
public class HistogramSlot {
    private final short slotTime;
    private final ResponseType responseType;


    public HistogramSlot(short slotTime, ResponseType responseType) {
        if (responseType == null) {
            throw new NullPointerException("responseType must not be null");
        }
        this.slotTime = slotTime;
        this.responseType = responseType;
    }

    // TODO : int를 short으로 바꿔야할 듯...
    public short getSlotTime() {
        return slotTime;
    }


    public ResponseType getResponseType() {
        return responseType;
    }

}
