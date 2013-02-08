package com.profiler.common.util;

import java.io.*;
import java.util.Date;

public class AnnotationTranscoder2 {

    private static final String DEFAULT_CHARSET = "UTF-8";

    // Special flags for specially handled types.
    static final int CODE_NULL = -1;
    static final int CODE_STRING = 0;
    static final int CODE_INT = 1;
    static final int CODE_LONG = 2;
    static final int CODE_BOOLEAN = 3;
    static final int CODE_BYTEARRAY = 4;
    static final int CODE_BYTE = 5;


    static final int CODE_FLOAT = 6;
    static final int CODE_DOUBLE = 7;

    static final int CODE_DATE = 8;

    static final int JAVA_SERIALIZED = 50;

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
            case CODE_BOOLEAN:
                return Boolean.valueOf(tu.decodeBoolean(data));
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
            case JAVA_SERIALIZED:
                return deserialize(data);

            default:
//				LOG.warn("Undecodeable with flags %x", flags);
        }
        return null;
    }

    public int getTypeCode(Object o) {
        if(o == null) {
            return CODE_NULL;
        }
        if (o instanceof String) {
            return CODE_STRING;
        } else if (o instanceof Long) {
            return CODE_LONG;
        } else if (o instanceof Integer) {
            return CODE_INT;
        } else if (o instanceof Boolean) {
            return CODE_BOOLEAN;
        } else if (o instanceof Date) {
            return CODE_DATE;
        } else if (o instanceof Byte) {
            return CODE_BYTE;
        } else if (o instanceof Float) {
            return CODE_FLOAT;
        } else if (o instanceof Double) {
            return CODE_DOUBLE;
        } else if (o instanceof byte[]) {
            return CODE_BYTEARRAY;
        } else {
            return JAVA_SERIALIZED;
        }
    }


    public byte[] encode(Object o, int typeCode) {
        switch (typeCode) {
            case CODE_STRING:
                return encodeString((String) o);
            case CODE_INT:
                return tu.encodeInt((Integer) o);
            case CODE_BOOLEAN:
                return tu.encodeBoolean((Boolean) o);
            case CODE_LONG:
                return tu.encodeLong((Long) o);
            case CODE_DATE:
                return tu.encodeLong(((Date) o).getTime());
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
            default:
                return serialize(o);
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
            b = tu.encodeBoolean((Boolean) o);
            type = CODE_BOOLEAN;
        } else if (o instanceof Date) {
            b = tu.encodeLong(((Date) o).getTime());
            type = CODE_DATE;
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
        } else {
            b = serialize(o);
            type = JAVA_SERIALIZED;
        }

        return new Encoded(type, b);
    }

    protected byte[] serialize(Object o) {
        if (o == null) {
            throw new NullPointerException("Can't serialize null");
        }
        byte[] rv = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(o);
            os.close();
            bos.close();
            rv = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Non-serializable object, cause=" + e.getMessage(), e);
        }
        return rv;
    }

    protected Object deserialize(byte[] in) {
        Object rv = null;
        try {
            if (in != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(in);
                ObjectInputStream is = new ObjectInputStream(bis);
                rv = is.readObject();
                is.close();
                bis.close();
            }
        } catch (IOException e) {
//			LOG.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
//			LOG.error(e.getMessage(), e);
        }
        return rv;
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
