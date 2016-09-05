package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.util.BitFieldUtils;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanBitFiled {

    // 1bit
    public static final int SET_APPLICATION_SERVICE_TYPE_ENCODING_STRATEGY = 0;
    public static final int SET_ROOT = 1;
    public static final int SET_ERROR_CODE = 2;
    public static final int SET_HAS_EXCEPTION = 3;
    public static final int SET_FLAG = 4;
    public static final int SET_LOGGING_TRANSACTION_INFO = 5;
    public static final int SET_ANNOTATION = 6;


    private static final long ROOT_PARENT_SPAN_ID = -1;
    // used : 7bit
    // reserved : 1 bit
    private byte bitField = 0;

    public static SpanBitFiled build(SpanBo spanBo) {
        if (spanBo == null) {
            throw new NullPointerException("spanBo must not be null");
        }
        final SpanBitFiled spanBitFiled = new SpanBitFiled();


        if (spanBo.getServiceType() == spanBo.getApplicationServiceType()) {
            spanBitFiled.setApplicationServiceTypeEncodingStrategy(ServiceTypeEncodingStrategy.PREV_EQUALS);
        } else {
            spanBitFiled.setApplicationServiceTypeEncodingStrategy(ServiceTypeEncodingStrategy.RAW);
        }

        if (spanBo.getParentSpanId() == ROOT_PARENT_SPAN_ID) {
            spanBitFiled.setRoot(true);
        }
        if (spanBo.getErrCode() != 0) {
            spanBitFiled.setErrorCode(true);
        }

        if (spanBo.hasException()) {
            spanBitFiled.setHasException(true);
        }

        if (spanBo.getFlag() != 0) {
            spanBitFiled.setFlag(true);
        }

        if (spanBo.getLoggingTransactionInfo() != LoggingInfo.NOT_LOGGED.getCode()) {
            spanBitFiled.setLoggingTransactionInfo(true);
        }
        if (CollectionUtils.isNotEmpty(spanBo.getAnnotationBoList())) {
            spanBitFiled.setAnnotation(true);
        }

        return spanBitFiled;
    }

    public SpanBitFiled() {
    }

    public SpanBitFiled(byte bitField) {
        this.bitField = bitField;
    }

    public byte getBitField() {
        return bitField;
    }

    // for test
    void maskAll() {
        bitField = -1;
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

    public ServiceTypeEncodingStrategy getApplicationServiceTypeEncodingStrategy() {
        final int set = getBit(SET_APPLICATION_SERVICE_TYPE_ENCODING_STRATEGY);
        switch (set) {
            case 0:
                return ServiceTypeEncodingStrategy.PREV_EQUALS;
            case 1:
                return ServiceTypeEncodingStrategy.RAW;
            default:
                throw new IllegalArgumentException("SET_APPLICATION_SERVICE_TYPE_ENCODING_STRATEGY");
        }
    }

    void setApplicationServiceTypeEncodingStrategy(ServiceTypeEncodingStrategy strategy) {
        switch (strategy) {
            case PREV_EQUALS:
                setBit(SET_APPLICATION_SERVICE_TYPE_ENCODING_STRATEGY, false);
                break;
            case RAW:
                setBit(SET_APPLICATION_SERVICE_TYPE_ENCODING_STRATEGY, true);
                break;
            default:
                throw new IllegalArgumentException("SET_APPLICATION_SERVICE_TYPE_ENCODING_STRATEGY");
        }

    }


    public boolean isRoot() {
        return testBit(SET_ROOT);
    }

    // for test
    void setRoot(boolean root) {
        setBit(SET_ROOT, root);
    }


    public boolean isSetErrorCode() {
        return testBit(SET_ERROR_CODE);
    }


    // for test
    void setErrorCode(boolean errorCode) {
        setBit(SET_ERROR_CODE, errorCode);
    }

    public boolean isSetHasException() {
        return testBit(SET_HAS_EXCEPTION);
    }

    // for test
    void setHasException(boolean hasException) {
        setBit(SET_HAS_EXCEPTION, hasException);
    }


    public boolean isSetFlag() {
        return testBit(SET_FLAG);
    }

    // for test
    void setFlag(boolean flag) {
        setBit(SET_FLAG, flag);
    }


    public boolean isSetLoggingTransactionInfo() {
        return testBit(SET_LOGGING_TRANSACTION_INFO);
    }

    // for test
    void setLoggingTransactionInfo(boolean loggingTransactionInfo) {
        setBit(SET_LOGGING_TRANSACTION_INFO, loggingTransactionInfo);
    }

    public boolean isSetAnnotation() {
        return testBit(SET_ANNOTATION);
    }


    public void setAnnotation(boolean annotation) {
        setBit(SET_ANNOTATION, annotation);
    }
}
