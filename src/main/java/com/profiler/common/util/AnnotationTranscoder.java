package com.profiler.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class AnnotationTranscoder {

    private static final String DEFAULT_CHARSET = "UTF-8";

    static final int SERIALIZED = 1;

    // Special flags for specially handled types.
    protected static final int SPECIAL_MASK = 0xff00;
    static final int SPECIAL_BOOLEAN = (1 << 8);
    static final int SPECIAL_INT = (2 << 8);
    static final int SPECIAL_LONG = (3 << 8);
    static final int SPECIAL_DATE = (4 << 8);
    static final int SPECIAL_BYTE = (5 << 8);
    static final int SPECIAL_FLOAT = (6 << 8);
    static final int SPECIAL_DOUBLE = (7 << 8);
    static final int SPECIAL_BYTEARRAY = (8 << 8);

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
        Object rv = null;

        int flags = dataType & SPECIAL_MASK;

        if ((dataType & SERIALIZED) != 0 && data != null) {
            rv = deserialize(data);
        } else if (flags != 0 && data != null) {
            switch (flags) {
                case SPECIAL_BOOLEAN:
                    rv = Boolean.valueOf(tu.decodeBoolean(data));
                    break;
                case SPECIAL_INT:
                    rv = new Integer(tu.decodeInt(data));
                    break;
                case SPECIAL_LONG:
                    rv = new Long(tu.decodeLong(data));
                    break;
                case SPECIAL_DATE:
                    rv = new Date(tu.decodeLong(data));
                    break;
                case SPECIAL_BYTE:
                    rv = new Byte(tu.decodeByte(data));
                    break;
                case SPECIAL_FLOAT:
                    rv = new Float(Float.intBitsToFloat(tu.decodeInt(data)));
                    break;
                case SPECIAL_DOUBLE:
                    rv = new Double(Double.longBitsToDouble(tu.decodeLong(data)));
                    break;
                case SPECIAL_BYTEARRAY:
                    rv = data;
                    break;
                default:
//				LOG.warn("Undecodeable with flags %x", flags);
            }
        } else {
            rv = decodeString(data);
        }
        return rv;
    }

    public int getTypeCode(Object o) {
        if (o instanceof String) {
            return 0;
        } else if (o instanceof Long) {
            return SPECIAL_LONG;
        } else if (o instanceof Integer) {
            return SPECIAL_INT;
        } else if (o instanceof Boolean) {
            return SPECIAL_BOOLEAN;
        } else if (o instanceof Date) {
            return SPECIAL_DATE;
        } else if (o instanceof Byte) {
            return SPECIAL_BYTE;
        } else if (o instanceof Float) {
            return SPECIAL_FLOAT;
        } else if (o instanceof Double) {
            return SPECIAL_DOUBLE;
        } else if (o instanceof byte[]) {
            return SPECIAL_BYTEARRAY;
        } else {
            return SERIALIZED;
        }
    }


    public byte[] encode(Object o, int typeCode) {
        switch (typeCode) {
            case 0:
                return encodeString((String) o);
            case SPECIAL_INT:
                return tu.encodeInt((Integer) o);
            case SPECIAL_BOOLEAN:
                return tu.encodeBoolean((Boolean) o);
            case SPECIAL_LONG:
                return tu.encodeLong((Long) o);
            case SPECIAL_DATE:
                return tu.encodeLong(((Date) o).getTime());
            case SPECIAL_BYTE:
                return tu.encodeByte((Byte) o);
            case SPECIAL_FLOAT:
                return tu.encodeInt(Float.floatToRawIntBits((Float) o));
            case SPECIAL_DOUBLE:
                return tu.encodeLong(Double.doubleToRawLongBits((Double) o));
            case SPECIAL_BYTEARRAY:
                return (byte[]) o;
            default:
                return serialize(o);
        }
    }

    Encoded encode(Object o) {
        byte[] b = null;
        int flags = 0;
        if (o instanceof String) {
            b = encodeString((String) o);
        } else if (o instanceof Long) {
            b = tu.encodeLong((Long) o);
            flags |= SPECIAL_LONG;
        } else if (o instanceof Integer) {
            b = tu.encodeInt((Integer) o);
            flags |= SPECIAL_INT;
        } else if (o instanceof Boolean) {
            b = tu.encodeBoolean((Boolean) o);
            flags |= SPECIAL_BOOLEAN;
        } else if (o instanceof Date) {
            b = tu.encodeLong(((Date) o).getTime());
            flags |= SPECIAL_DATE;
        } else if (o instanceof Byte) {
            b = tu.encodeByte((Byte) o);
            flags |= SPECIAL_BYTE;
        } else if (o instanceof Float) {
            b = tu.encodeInt(Float.floatToRawIntBits((Float) o));
            flags |= SPECIAL_FLOAT;
        } else if (o instanceof Double) {
            b = tu.encodeLong(Double.doubleToRawLongBits((Double) o));
            flags |= SPECIAL_DOUBLE;
        } else if (o instanceof byte[]) {
            b = (byte[]) o;
            flags |= SPECIAL_BYTEARRAY;
        } else {
            b = serialize(o);
            flags |= SERIALIZED;
        }

        assert b != null;

        return new Encoded(flags, b);
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
        String rv = null;
        try {
            if (data != null) {
                rv = new String(data, DEFAULT_CHARSET);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return rv;
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
