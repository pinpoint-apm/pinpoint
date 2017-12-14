package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.BitFieldUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventBitField {


    public static final int SET_ANNOTATION = 0;
    public static final int SET_HAS_EXCEPTION = 1;
    public static final int SET_NEXT_ASYNCID = 2;
    public static final int SET_ASYNCID = 3;
    public static final int SET_NEXT_SPANID = 4;
    public static final int SET_ENDPOINT = 5;
    public static final int SET_DESTINATIONID = 6;
    public static final int SET_RPC = 7;
//  firstSpan bitField -----------------------------------------------
    public static final int START_ELAPSED_ENCODING_STRATEGY = 8;
    public static final int SERVICE_TYPE_ENCODING_STRATEGY = 9;
    public static final int SEQUENCE_ENCODING_STRATEGY = 10;
    public static final int DEPTH_ENCODING_STRATEGY = 11;

    public static final int API_ENCODING_STRATEGY = 13;


    private short bitField = 0;


    public static SpanEventBitField buildFirst(SpanEventBo spanEventBo) {
        if (spanEventBo == null) {
            throw new NullPointerException("spanEventBo must not be null");
        }
        final SpanEventBitField bitFiled = new SpanEventBitField();

        if (spanEventBo.getRpc() != null) {
            bitFiled.setRpc(true);
        }
        if (spanEventBo.getEndPoint() != null) {
            bitFiled.setEndPoint(true);
        }

        if (spanEventBo.getDestinationId() != null) {
            bitFiled.setDestinationId(true);
        }

        if (spanEventBo.getNextSpanId() != -1) {
            bitFiled.setNextSpanId(true);
        }

        if (spanEventBo.hasException()) {
            bitFiled.setHasException(true);
        }

        final List<AnnotationBo> annotationBoList = spanEventBo.getAnnotationBoList();
        if (CollectionUtils.isNotEmpty(annotationBoList)) {
            bitFiled.setAnnotation(true);
        }

        if (spanEventBo.getNextAsyncId() != -1)  {
            bitFiled.setNextAsyncId(true);
        }


        if (spanEventBo.getAsyncId() == -1 && spanEventBo.getAsyncSequence() == -1) {
            bitFiled.setAsyncId(false);
        } else {
            bitFiled.setAsyncId(true);
        }
        return bitFiled;
    }

    public static SpanEventBitField build(SpanEventBo spanEventBo, SpanEventBo prevSpanEventBo) {
        if (spanEventBo == null) {
            throw new NullPointerException("spanEventBo must not be null");
        }
        if (prevSpanEventBo == null) {
            throw new NullPointerException("prevSpanEventBo must not be null");
        }

        final SpanEventBitField bitFiled = buildFirst(spanEventBo);

        if (spanEventBo.getStartElapsed() == prevSpanEventBo.getStartElapsed()) {
            bitFiled.setStartElapsedEncodingStrategy(StartElapsedTimeEncodingStrategy.PREV_EQUALS);
        } else {
            bitFiled.setStartElapsedEncodingStrategy(StartElapsedTimeEncodingStrategy.PREV_DELTA);
        }

        // sequence prev: 5 current: 6 = 6 - 5= delta 1
        final short sequenceDelta = (short) (spanEventBo.getSequence() - prevSpanEventBo.getSequence());
        if (sequenceDelta == 1) {
            bitFiled.setSequenceEncodingStrategy(SequenceEncodingStrategy.PREV_ADD1);
        } else {
            bitFiled.setSequenceEncodingStrategy(SequenceEncodingStrategy.PREV_DELTA);
        }

        if (spanEventBo.getDepth() == prevSpanEventBo.getDepth()) {
            bitFiled.setDepthEncodingStrategy(DepthEncodingStrategy.PREV_EQUALS);
        } else {
            bitFiled.setDepthEncodingStrategy(DepthEncodingStrategy.RAW);
        }


        if (prevSpanEventBo.getServiceType() == spanEventBo.getServiceType()) {
            bitFiled.setServiceTypeEncodingStrategy(ServiceTypeEncodingStrategy.PREV_EQUALS);
        } else {
            bitFiled.setServiceTypeEncodingStrategy(ServiceTypeEncodingStrategy.RAW);
        }


        return bitFiled;
    }

    public SpanEventBitField() {
    }

    public SpanEventBitField(short bitField) {
        this.bitField = bitField;
    }

    // for test
    void maskAll() {
        bitField = -1;
    }

    public short getBitField() {
        return bitField;
    }

    private void setBit(int position, boolean value) {
        this.bitField = BitFieldUtils.setBit(bitField, position, value);
    }

    private boolean testBit(int position) {
        return BitFieldUtils.testBit(bitField, position);
    }

    private int getBit(int position) {
        return BitFieldUtils.getBit(bitField, position);
    }


    public boolean isSetHasException() {
        return testBit(SET_HAS_EXCEPTION);
    }

    void setHasException(boolean hasException) {
        setBit(SET_HAS_EXCEPTION, hasException);
    }


    public boolean isSetAnnotation() {
        return testBit(SET_ANNOTATION);
    }

    void setAnnotation(boolean annotation) {
        setBit(SET_ANNOTATION, annotation);
    }


    public boolean isSetNextAsyncId() {
        return testBit(SET_NEXT_ASYNCID);
    }

    void setNextAsyncId(boolean nextAsyncSpanId) {
        setBit(SET_NEXT_ASYNCID, nextAsyncSpanId);
    }


    public boolean isSetNextSpanId() {
        return testBit(SET_NEXT_SPANID);
    }

    void setNextSpanId(boolean nextSpanId) {
        setBit(SET_NEXT_SPANID, nextSpanId);
    }

    public boolean isSetEndPoint() {
        return testBit(SET_ENDPOINT);
    }

    void setEndPoint(boolean endPoint) {
        setBit(SET_ENDPOINT, endPoint);
    }

    public boolean isSetDestinationId() {
        return testBit(SET_DESTINATIONID);
    }

    void setDestinationId(boolean destinationId) {
        setBit(SET_DESTINATIONID, destinationId);
    }


    public boolean isSetRpc() {
        return testBit(SET_RPC);
    }

    void setRpc(boolean rpc) {
        setBit(SET_RPC, rpc);
    }

    public boolean isSetAsyncId() {
        return testBit(SET_ASYNCID);
    }

    void setAsyncId(boolean asyncId) {
        setBit(SET_ASYNCID, asyncId);
    }



    public StartElapsedTimeEncodingStrategy getStartElapsedEncodingStrategy() {
        final int set = getBit(START_ELAPSED_ENCODING_STRATEGY);
        switch (set) {
            case 0:
                return StartElapsedTimeEncodingStrategy.PREV_EQUALS;
            case 1:
                return StartElapsedTimeEncodingStrategy.PREV_DELTA;
            default:
                throw new IllegalArgumentException("SERVICE_TYPE_ENCODING_STRATEGY");
        }
    }

    void setStartElapsedEncodingStrategy(StartElapsedTimeEncodingStrategy strategy) {
        switch (strategy) {
            case PREV_EQUALS:
                setBit(START_ELAPSED_ENCODING_STRATEGY, false);
                break;
            case PREV_DELTA:
                setBit(START_ELAPSED_ENCODING_STRATEGY, true);
                break;
            default:
                throw new IllegalArgumentException("START_ELAPSED_ENCODING_STRATEGY");
        }
    }



    public ServiceTypeEncodingStrategy getServiceTypeEncodingStrategy() {
        final int set = getBit(SERVICE_TYPE_ENCODING_STRATEGY);
        switch (set) {
            case 0:
                return ServiceTypeEncodingStrategy.PREV_EQUALS;
            case 1:
                return ServiceTypeEncodingStrategy.RAW;
            default:
                throw new IllegalArgumentException("SERVICE_TYPE_ENCODING_STRATEGY");
        }
    }

    void setServiceTypeEncodingStrategy(ServiceTypeEncodingStrategy strategy) {
        switch (strategy) {
            case PREV_EQUALS:
                setBit(SERVICE_TYPE_ENCODING_STRATEGY, false);
                break;
            case RAW:
                setBit(SERVICE_TYPE_ENCODING_STRATEGY, true);
                break;
            default:
                throw new IllegalArgumentException("SERVICE_TYPE_ENCODING_STRATEGY");
        }
    }

    public SequenceEncodingStrategy getSequenceEncodingStrategy() {
        final int set = getBit(SEQUENCE_ENCODING_STRATEGY);
        switch (set) {
            case 0:
                return SequenceEncodingStrategy.PREV_ADD1;
            case 1:
                return SequenceEncodingStrategy.PREV_DELTA;
            default:
                throw new IllegalArgumentException("SEQUENCE_ENCODING_STRATEGY");
        }
    }


    void setSequenceEncodingStrategy(SequenceEncodingStrategy strategy) {
        switch (strategy) {
            case PREV_ADD1:
                setBit(SEQUENCE_ENCODING_STRATEGY, false);
                break;
            case PREV_DELTA:
                setBit(SEQUENCE_ENCODING_STRATEGY, true);
                break;
            default:
                throw new IllegalArgumentException("SEQUENCE_ENCODING_STRATEGY");
        }
    }

    public DepthEncodingStrategy getDepthEncodingStrategy() {
        final int set = getBit(DEPTH_ENCODING_STRATEGY);
        switch (set) {
            case 0:
                return DepthEncodingStrategy.PREV_EQUALS;
            case 1:
                return DepthEncodingStrategy.RAW;
            default:
                throw new IllegalArgumentException("DEPTH_ENCODING_STRATEGY");
        }
    }

    void setDepthEncodingStrategy(DepthEncodingStrategy strategy) {
        switch (strategy) {
            case PREV_EQUALS:
                setBit(DEPTH_ENCODING_STRATEGY, false);
                break;
            case RAW:
                setBit(DEPTH_ENCODING_STRATEGY, true);
                break;
            default:
                throw new IllegalArgumentException("SEQUENCE_ENCODING_STRATEGY");
        }
    }


}
