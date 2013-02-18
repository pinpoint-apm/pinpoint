package com.profiler.common.util;


import com.profiler.common.dto.thrift.Annotation;

import java.io.*;
import java.util.Date;

public class AnnotationTranscoder {

    private static final String DEFAULT_CHARSET = "UTF-8";

    public static final int CODE_STRING = 0;
    static final int CODE_NULL = 1;
    static final int CODE_INT = 2;
    static final int CODE_LONG = 3;

    static final int CODE_BOOLEAN_TRUE = 4;
    static final int CODE_BOOLEAN_FALSE = 5;

    static final int CODE_BYTEARRAY = 6;
    static final int CODE_BYTE = 7;

    static final int CODE_FLOAT = 8;
    static final int CODE_DOUBLE = 9;

    static final int CODE_DATE = 10;

    protected final TranscoderUtils tu = new TranscoderUtils(true);




    public static final class Encoded {

        private final int valueType;
        private final byte[] bytes;

        public Encoded(int valueType, byte[] bytes) {
            this.valueType = valueType;
            this.bytes = bytes;
        }

        public int getValueType() {
            return valueType;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }

    public Object decode(int dataType, byte[] data) {
        switch (dataType) {
            case CODE_STRING:
                return decodeString(data);
            case CODE_BOOLEAN_TRUE:
                return Boolean.TRUE;
            case CODE_BOOLEAN_FALSE:
                return Boolean.FALSE;
            case CODE_INT:
                return new Integer(tu.decodeInt(data));
            case CODE_LONG:
                return new Long(tu.decodeLong(data));
            case CODE_DATE:
                return new Date(tu.decodeLong(data));
            case CODE_BYTE:
                return new Byte(tu.decodeByte(data));
            case CODE_FLOAT:
                return new Float(Float.intBitsToFloat(tu.decodeInt(data)));
            case CODE_DOUBLE:
                return new Double(Double.longBitsToDouble(tu.decodeLong(data)));
            case CODE_BYTEARRAY:
                return data;
            case CODE_NULL:
                return null;


            default:
//				LOG.warn("Undecodeable with flags %x", flags);
        }
        return null;
    }

    public int getTypeCode(Object o) {
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
        } else if (o instanceof Float) {
            return CODE_FLOAT;
        } else if (o instanceof Double) {
            return CODE_DOUBLE;
        } else if (o instanceof byte[]) {
            return CODE_BYTEARRAY;
        } else if (o instanceof Date) {
            return CODE_DATE;
        }
        return 0;
    }

    public Object getMappingValue(Annotation annotation) {
        if (annotation.isSetStringValue()) {
            return annotation.getStringValue();
        } else if(annotation.isSetIntValue()) {
            return annotation.getIntValue();
        } else if(annotation.isSetLongValue()) {
            return annotation.getLongValue();
        } else if(annotation.isSetBoolValue()) {
            return annotation.isBoolValue();
        } else if(annotation.isSetByteValue()) {
            return annotation.getByteValue();
        } else if(annotation.isSetDoubleValue()) {
            return annotation.getDoubleValue();
        } else if(annotation.isSetBinaryValue()) {
            return annotation.getBinaryValue();
        }
        return null;
    }

    public void mappingValue(Object o, Annotation annotation) {
        if (o == null) {
            return;
        }
        if (o instanceof String) {
            annotation.setStringValue((String) o);
            return;
        } else if (o instanceof Integer) {
            annotation.setIntValue((Integer) o);
            return;
        } else if (o instanceof Long) {
            annotation.setLongValue((Long) o);
            return;
        } else if (o instanceof Boolean) {
            annotation.setBoolValue((Boolean) o);
            return;
        } else if (o instanceof Byte) {
            annotation.setByteValue((Byte) o);
            return;
        } else if (o instanceof Float) {
            annotation.setDoubleValue((Float) o);
            return;
        } else if (o instanceof Double) {
            annotation.setDoubleValue((Double) o);
            return;
        } else if (o instanceof byte[]) {
            annotation.setBinaryValue((byte[]) o);
            return;
        } else if (o instanceof Short) {
            annotation.setShortValue((Short) o);
            return;
        }
        String str = o.toString();
        annotation.setStringValue(str);
        return;
    }

    public byte[] encode(Object o, int typeCode) {
        switch (typeCode) {
            case CODE_STRING:
                return encodeString((String) o);
            case CODE_INT:
                return tu.encodeInt((Integer) o);
            case CODE_BOOLEAN_TRUE:
                return null;
            case CODE_BOOLEAN_FALSE:
                return null;
            case CODE_LONG:
                return tu.encodeLong((Long) o);
            case CODE_BYTE:
                return tu.encodeByte((Byte) o);
            case CODE_FLOAT:
                return tu.encodeInt(Float.floatToRawIntBits((Float) o));
            case CODE_DOUBLE:
                return tu.encodeLong(Double.doubleToRawLongBits((Double) o));
            case CODE_BYTEARRAY:
                return (byte[]) o;
            case CODE_NULL:
                return null;
            case CODE_DATE:
                return tu.encodeLong(((Date) o).getTime());
            default:
                String str = o.toString();
                return encodeString(str);
        }
    }

    Encoded encode(Object o) {
        byte[] b = null;
        int type = 0;
        if (o instanceof String) {
            b = encodeString((String) o);
            type = CODE_STRING;
        } else if (o instanceof Long) {
            b = tu.encodeLong((Long) o);
            type = CODE_LONG;
        } else if (o instanceof Integer) {
            b = tu.encodeInt((Integer) o);
            type = CODE_INT;
        } else if (o instanceof Boolean) {
            if(Boolean.TRUE.equals(o)) {
                type = CODE_BOOLEAN_TRUE;
            } else {
                type = CODE_BOOLEAN_FALSE;
            }
        } else if (o instanceof Byte) {
            b = tu.encodeByte((Byte) o);
            type = CODE_BYTE;
        } else if (o instanceof Float) {
            b = tu.encodeInt(Float.floatToRawIntBits((Float) o));
            type = CODE_FLOAT;
        } else if (o instanceof Double) {
            b = tu.encodeLong(Double.doubleToRawLongBits((Double) o));
            type = CODE_DOUBLE;
        } else if (o instanceof byte[]) {
            b = (byte[]) o;
            type = CODE_BYTEARRAY;
        } else if (o instanceof Date) {
            b = tu.encodeLong(((Date) o).getTime());
            type = CODE_DATE;
        } else {
            String str = o.toString();
            b = encodeString(str);
            type = CODE_STRING;
        }

        return new Encoded(type, b);
    }

    /**
     * Decode the string with the current character set.
     */
    protected String decodeString(byte[] data) {
        if(data == null) {
            return null;
        }
        try {
            return new String(data, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encode a string into the current character set.
     */
    protected byte[] encodeString(String in) {
        byte[] rv = null;
        try {
            rv = in.getBytes(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return rv;
    }
}
