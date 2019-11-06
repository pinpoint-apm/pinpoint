package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanBitFiledTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testRoot_1() throws Exception {
        SpanBo spanBo = new SpanBo();
        spanBo.setParentSpanId(-1);

        SpanBitFiled spanBitFiled = SpanBitFiled.build(spanBo);
        Assert.assertTrue(spanBitFiled.isRoot());

        spanBitFiled.setRoot(false);
        Assert.assertFalse(spanBitFiled.isRoot());

    }

    @Test
    public void testRoot_2() throws Exception {
        SpanBo spanBo = new SpanBo();
        spanBo.setParentSpanId(0);

        SpanBitFiled spanBitFiled = SpanBitFiled.build(spanBo);
        Assert.assertFalse(spanBitFiled.isRoot());

        spanBitFiled.maskAll();
        spanBitFiled.setRoot(false);
        Assert.assertFalse(spanBitFiled.isRoot());
    }

    @Test
    public void testErrorCode_1() {
        SpanBo spanBo = new SpanBo();
        spanBo.setErrCode(1);
        SpanBitFiled spanBitFiled = SpanBitFiled.build(spanBo);

        Assert.assertTrue(spanBitFiled.isSetErrorCode());

        spanBitFiled.setErrorCode(false);
        Assert.assertFalse(spanBitFiled.isSetErrorCode());
    }

    @Test
    public void testErrorCode_2() {
        SpanBo spanBo = new SpanBo();
        spanBo.setErrCode(0);
        SpanBitFiled spanBitFiled = SpanBitFiled.build(spanBo);

        Assert.assertFalse(spanBitFiled.isSetErrorCode());

        spanBitFiled.maskAll();
        spanBitFiled.setErrorCode(false);
        Assert.assertFalse(spanBitFiled.isSetErrorCode());
    }

    @Test
    public void testApplicationServiceTypeEncodingStrategy_PREV_TYPE_EQUALS() {
        SpanBo spanBo = new SpanBo();
        spanBo.setServiceType((short) 1000);
        spanBo.setApplicationServiceType((short) 1000);

        SpanBitFiled spanBitFiled = SpanBitFiled.build(spanBo);

        Assert.assertEquals(spanBitFiled.getApplicationServiceTypeEncodingStrategy(), ServiceTypeEncodingStrategy.PREV_EQUALS);
    }

    @Test
    public void testApplicationServiceTypeEncodingStrategy_RAW() {
        SpanBo spanBo = new SpanBo();
        spanBo.setServiceType((short) 1000);
        spanBo.setApplicationServiceType((short) 2000);

        SpanBitFiled spanBitFiled = SpanBitFiled.build(spanBo);

        Assert.assertEquals(spanBitFiled.getApplicationServiceTypeEncodingStrategy(), ServiceTypeEncodingStrategy.RAW);

        spanBitFiled.maskAll();
        Assert.assertEquals(spanBitFiled.getApplicationServiceTypeEncodingStrategy(), ServiceTypeEncodingStrategy.RAW);
        spanBitFiled.setApplicationServiceTypeEncodingStrategy(ServiceTypeEncodingStrategy.PREV_EQUALS);
        Assert.assertEquals(spanBitFiled.getApplicationServiceTypeEncodingStrategy(), ServiceTypeEncodingStrategy.PREV_EQUALS);
    }


    @Test
    public void testHasException_1() {
        SpanBo spanBo = new SpanBo();
        spanBo.setExceptionInfo(1, "error");

        SpanBitFiled spanBitFiled = SpanBitFiled.build(spanBo);

        Assert.assertTrue(spanBitFiled.isSetHasException());

        spanBitFiled.setHasException(false);
        Assert.assertFalse(spanBitFiled.isSetHasException());
    }

    @Test
    public void testHasException_2() {
        SpanBo spanBo = new SpanBo();

        SpanBitFiled spanBitFiled = SpanBitFiled.build(spanBo);

        Assert.assertFalse(spanBitFiled.isSetHasException());

        spanBitFiled.maskAll();
        spanBitFiled.setHasException(false);
        Assert.assertFalse(spanBitFiled.isSetHasException());
    }



    @Test
    public void testLoggingTransactionInfo_1() {
        SpanBo spanBo = new SpanBo();
        spanBo.setLoggingTransactionInfo(LoggingInfo.INFO.getCode());

        SpanBitFiled spanBitFiled = SpanBitFiled.build(spanBo);

        Assert.assertTrue(spanBitFiled.isSetLoggingTransactionInfo());

        spanBitFiled.setLoggingTransactionInfo(false);
        Assert.assertFalse(spanBitFiled.isSetLoggingTransactionInfo());
    }

    @Test
    public void testLoggingTransactionInfo_2() {
        SpanBo spanBo = new SpanBo();

        SpanBitFiled spanBitFiled = SpanBitFiled.build(spanBo);

        Assert.assertFalse(spanBitFiled.isSetLoggingTransactionInfo());

        spanBitFiled.maskAll();
        spanBitFiled.setLoggingTransactionInfo(false);
        Assert.assertFalse(spanBitFiled.isSetLoggingTransactionInfo());
    }

}