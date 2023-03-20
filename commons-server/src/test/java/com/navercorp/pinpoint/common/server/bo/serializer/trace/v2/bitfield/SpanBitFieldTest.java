package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanBitFieldTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testRoot_1() throws Exception {
        SpanBo spanBo = new SpanBo();
        spanBo.setParentSpanId(-1);

        SpanBitField spanBitField = SpanBitField.build(spanBo);
        Assertions.assertTrue(spanBitField.isRoot());

        spanBitField.setRoot(false);
        Assertions.assertFalse(spanBitField.isRoot());

    }

    @Test
    public void testRoot_2() throws Exception {
        SpanBo spanBo = new SpanBo();
        spanBo.setParentSpanId(0);

        SpanBitField spanBitField = SpanBitField.build(spanBo);
        Assertions.assertFalse(spanBitField.isRoot());

        spanBitField.maskAll();
        spanBitField.setRoot(false);
        Assertions.assertFalse(spanBitField.isRoot());
    }

    @Test
    public void testErrorCode_1() {
        SpanBo spanBo = new SpanBo();
        spanBo.setErrCode(1);
        SpanBitField spanBitField = SpanBitField.build(spanBo);

        Assertions.assertTrue(spanBitField.isSetErrorCode());

        spanBitField.setErrorCode(false);
        Assertions.assertFalse(spanBitField.isSetErrorCode());
    }

    @Test
    public void testErrorCode_2() {
        SpanBo spanBo = new SpanBo();
        spanBo.setErrCode(0);
        SpanBitField spanBitField = SpanBitField.build(spanBo);

        Assertions.assertFalse(spanBitField.isSetErrorCode());

        spanBitField.maskAll();
        spanBitField.setErrorCode(false);
        Assertions.assertFalse(spanBitField.isSetErrorCode());
    }

    @Test
    public void testApplicationServiceTypeEncodingStrategy_PREV_TYPE_EQUALS() {
        SpanBo spanBo = new SpanBo();
        spanBo.setServiceType((short) 1000);
        spanBo.setApplicationServiceType((short) 1000);

        SpanBitField spanBitField = SpanBitField.build(spanBo);

        Assertions.assertEquals(spanBitField.getApplicationServiceTypeEncodingStrategy(), ServiceTypeEncodingStrategy.PREV_EQUALS);
    }

    @Test
    public void testApplicationServiceTypeEncodingStrategy_RAW() {
        SpanBo spanBo = new SpanBo();
        spanBo.setServiceType((short) 1000);
        spanBo.setApplicationServiceType((short) 2000);

        SpanBitField spanBitField = SpanBitField.build(spanBo);

        Assertions.assertEquals(spanBitField.getApplicationServiceTypeEncodingStrategy(), ServiceTypeEncodingStrategy.RAW);

        spanBitField.maskAll();
        Assertions.assertEquals(spanBitField.getApplicationServiceTypeEncodingStrategy(), ServiceTypeEncodingStrategy.RAW);
        spanBitField.setApplicationServiceTypeEncodingStrategy(ServiceTypeEncodingStrategy.PREV_EQUALS);
        Assertions.assertEquals(spanBitField.getApplicationServiceTypeEncodingStrategy(), ServiceTypeEncodingStrategy.PREV_EQUALS);
    }


    @Test
    public void testHasException_1() {
        SpanBo spanBo = new SpanBo();
        spanBo.setExceptionInfo(1, "error");

        SpanBitField spanBitField = SpanBitField.build(spanBo);

        Assertions.assertTrue(spanBitField.isSetHasException());

        spanBitField.setHasException(false);
        Assertions.assertFalse(spanBitField.isSetHasException());
    }

    @Test
    public void testHasException_2() {
        SpanBo spanBo = new SpanBo();

        SpanBitField spanBitField = SpanBitField.build(spanBo);

        Assertions.assertFalse(spanBitField.isSetHasException());

        spanBitField.maskAll();
        spanBitField.setHasException(false);
        Assertions.assertFalse(spanBitField.isSetHasException());
    }


    @Test
    public void testLoggingTransactionInfo_1() {
        SpanBo spanBo = new SpanBo();
        spanBo.setLoggingTransactionInfo(LoggingInfo.INFO.getCode());

        SpanBitField spanBitField = SpanBitField.build(spanBo);

        Assertions.assertTrue(spanBitField.isSetLoggingTransactionInfo());

        spanBitField.setLoggingTransactionInfo(false);
        Assertions.assertFalse(spanBitField.isSetLoggingTransactionInfo());
    }

    @Test
    public void testLoggingTransactionInfo_2() {
        SpanBo spanBo = new SpanBo();

        SpanBitField spanBitField = SpanBitField.build(spanBo);

        Assertions.assertFalse(spanBitField.isSetLoggingTransactionInfo());

        spanBitField.maskAll();
        spanBitField.setLoggingTransactionInfo(false);
        Assertions.assertFalse(spanBitField.isSetLoggingTransactionInfo());
    }

}