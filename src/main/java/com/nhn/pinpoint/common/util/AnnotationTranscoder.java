package com.nhn.pinpoint.common.util;


import com.nhn.pinpoint.common.bo.IntStringValue;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.thrift.dto.TAnnotationValue;
import com.nhn.pinpoint.thrift.dto.TIntStringValue;

public class AnnotationTranscoder {

    static final byte CODE_STRING = 0;
    static final byte CODE_NULL = 1;
    static final byte CODE_INT = 2;
    static final byte CODE_LONG = 3;

    static final byte CODE_BOOLEAN_TRUE = 4;
    static final byte CODE_BOOLEAN_FALSE = 5;

    static final byte CODE_BYTEARRAY = 6;
    static final byte CODE_BYTE = 7;

    static final byte CODE_SHORT = 8;
    static final byte CODE_FLOAT = 9;
    static final byte CODE_DOUBLE = 10;
    static final byte CODE_TOSTRING = 11;
    // multivalue
    static final byte CODE_TINTSTRINGVALUE = 20;

    protected final TranscoderUtils tu = new TranscoderUtils(true);


    public Object getMappingValue(TAnnotation annotation) {
        final TAnnotationValue value = annotation.getValue();
        if (value == null) {
            return null;
        }
        return value.getFieldValue();
    }


    public Object decode(byte dataType, byte[] data) {
        switch (dataType) {
            case CODE_STRING:
                return decodeString(data);
            case CODE_BOOLEAN_TRUE:
                return Boolean.TRUE;
            case CODE_BOOLEAN_FALSE:
                return Boolean.FALSE;
            case CODE_INT:
                return tu.decodeInt(data);
            case CODE_LONG:
                return tu.decodeLong(data);
            case CODE_BYTE:
                return tu.decodeByte(data);
            case CODE_SHORT:
                return tu.decodeShort(data);
            case CODE_FLOAT:
                return Float.intBitsToFloat(tu.decodeInt(data));
            case CODE_DOUBLE:
                return Double.longBitsToDouble(tu.decodeLong(data));
            case CODE_BYTEARRAY:
                return data;
            case CODE_NULL:
                return null;
            case CODE_TOSTRING:
                return decodeString(data);
            case CODE_TINTSTRINGVALUE:
                return decodeTIntStringValue(data);
        }
        throw new IllegalArgumentException("unsupported DataType:" + dataType);
    }

    public byte getTypeCode(Object o) {
        if (o == null) {
            return CODE_NULL;
        }
        if (o instanceof String) {
            return CODE_STRING;
        } else if (o instanceof Long) {
            return CODE_LONG;
        } else if (o instanceof Integer) {
            return CODE_INT;
        } else if (o instanceof Boolean) {
            if (Boolean.TRUE.equals(o)) {
                return CODE_BOOLEAN_TRUE;
            }
            return CODE_BOOLEAN_FALSE;
        } else if (o instanceof Byte) {
            return CODE_BYTE;
        } else if (o instanceof Short) {
            return CODE_SHORT;
        } else if (o instanceof Float) {
            // thrift에서 지원안함.
            return CODE_FLOAT;
        } else if (o instanceof Double) {
            return CODE_DOUBLE;
        } else if (o instanceof byte[]) {
            return CODE_BYTEARRAY;
        } else if(o instanceof TIntStringValue) {
            return CODE_TINTSTRINGVALUE;
        }
        return CODE_TOSTRING;
    }

    public byte[] encode(Object o, int typeCode) {
        switch (typeCode) {
            case CODE_STRING:
                return encodeString((String) o);
            case CODE_INT:
                return tu.encodeInt((Integer) o);
            case CODE_BOOLEAN_TRUE:
                return new byte[0];
            case CODE_BOOLEAN_FALSE:
                return new byte[0];
            case CODE_LONG:
                return tu.encodeLong((Long) o);
            case CODE_BYTE:
                return tu.encodeByte((Byte) o);
            case CODE_SHORT:
                return tu.encodeShort((Short) o);
            case CODE_FLOAT:
                return tu.encodeInt(Float.floatToRawIntBits((Float) o));
            case CODE_DOUBLE:
                return tu.encodeLong(Double.doubleToRawLongBits((Double) o));
            case CODE_BYTEARRAY:
                return (byte[]) o;
            case CODE_NULL:
                return null;
            case CODE_TOSTRING:
                final String str = o.toString();
                return encodeString(str);
            case CODE_TINTSTRINGVALUE:
                return encodeTIntStringValue(o);
        }
        throw new IllegalArgumentException("unsupported DataType:" + typeCode + " data:" + o);
    }

    private Object decodeTIntStringValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final int intValue = buffer.readSVarInt();
        final String stringValue  = BytesUtils.toString(buffer.readPrefixedBytes());
        return new IntStringValue(intValue, stringValue);
    }

    private byte[] encodeTIntStringValue(Object value) {
        final TIntStringValue tIntStringValue = (TIntStringValue) value;
        final int intValue = tIntStringValue.getIntValue();
        final byte[] stringValue = BytesUtils.getBytes(tIntStringValue.getStringValue());
        final Buffer buffer = new AutomaticBuffer(stringValue.length + 4 + 8);
        buffer.putSVar(intValue);
        buffer.putPrefixedBytes(stringValue);
        return buffer.getBuffer();
    }


    /**
     * Decode the string with the current character set.
     */
    protected String decodeString(byte[] data) {
        return BytesUtils.toString(data);
    }

    /**
     * Encode a string into the current character set.
     */
    protected byte[] encodeString(String in) {
        return BytesUtils.getBytes(in);
    }
}
