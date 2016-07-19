package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield;

import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield.SpanEventBitField;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventBitFieldTest {
    @Test
    public void isSetHasException() throws Exception {

    }

    @Test
    public void setHasException_shortToByteCasting() throws Exception {
        SpanEventBitField field = new SpanEventBitField();
        field.setHasException(true);

        byte byteField = (byte) field.getBitField();

        SpanEventBitField byteCastField = new SpanEventBitField(byteField);
        Assert.assertTrue(byteCastField.isSetHasException());


    }

}