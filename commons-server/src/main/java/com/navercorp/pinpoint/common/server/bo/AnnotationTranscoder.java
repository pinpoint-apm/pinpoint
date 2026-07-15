/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo;


import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.annotation.BinaryAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.BooleanAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.ByteAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.DataTypeAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.DoubleAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.FloatAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.IntAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.LongAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.NullAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.ShortAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.StringAnnotationBo;
import com.navercorp.pinpoint.common.server.util.BitFieldUtils;
import com.navercorp.pinpoint.common.util.BytesStringStringValue;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;


/**
 * @author emeroad
 * @author jaehong.kim
 */
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
    static final byte CODE_INT_STRING = 20;
    static final byte CODE_INT_STRING_STRING = 21;
    static final byte CODE_LONG_INT_INT_BYTE_BYTE_STRING = 22;
    static final byte CODE_INT_BOOLEAN_INT_BOOLEAN = 23;
    static final byte CODE_STRING_STRING = 24;
    static final byte CODE_BYTES_STRING_STRING = 25;

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];


    public Object decode(final byte dataType, final byte[] data) {
        return switch (dataType) {
            case CODE_STRING -> decodeString(data);
            case CODE_BOOLEAN_TRUE -> Boolean.TRUE;
            case CODE_BOOLEAN_FALSE -> Boolean.FALSE;
            case CODE_INT -> BytesUtils.bytesToSVar32(data, 0);
            case CODE_LONG -> BytesUtils.bytesToSVar64(data, 0);
            case CODE_BYTE -> data[0];
            case CODE_SHORT ->
                // need short casting
                    (short) BytesUtils.bytesToSVar32(data, 0);
            case CODE_FLOAT -> Float.intBitsToFloat(ByteArrayUtils.bytesToInt(data, 0));
            case CODE_DOUBLE -> Double.longBitsToDouble(ByteArrayUtils.bytesToLong(data, 0));
            case CODE_BYTEARRAY -> data;
            case CODE_NULL -> null;
            case CODE_TOSTRING -> decodeString(data);
            case CODE_INT_STRING -> decodeIntStringValue(data);
            case CODE_INT_STRING_STRING -> decodeIntStringStringValue(data);
            case CODE_STRING_STRING -> decodeStringStringValue(data);
            case CODE_LONG_INT_INT_BYTE_BYTE_STRING -> decodeLongIntIntByteByteStringValue(data);
            case CODE_INT_BOOLEAN_INT_BOOLEAN -> decodeIntBooleanIntBooleanValue(data);
            case CODE_BYTES_STRING_STRING -> decodeBytesStringStringValue(data);
            default -> throw new IllegalArgumentException("unsupported DataType:" + dataType);
        };
    }

    /**
     * @deprecated Since 4.0.0. Use {@link #getEncoder(Object)}, which resolves
     * the type code and the encoding logic with a single type dispatch.
     */
    @Deprecated
    public byte getTypeCode(Object o) {
        if (o == null) {
            return CODE_NULL;
        }
        if (o instanceof Number) {
            if (o instanceof Long) {
                return CODE_LONG;
            } else if (o instanceof Integer) {
                return CODE_INT;
            } else if (o instanceof Short) {
                return CODE_SHORT;
            } else if (o instanceof Float) {
                // not supported by thrift
                return CODE_FLOAT;
            } else if (o instanceof Double) {
                return CODE_DOUBLE;
            } else if (o instanceof Byte) {
                return CODE_BYTE;
            }
        }
        if (o instanceof String) {
            return CODE_STRING;
        } else if (o instanceof Boolean) {
            if (Boolean.TRUE.equals(o)) {
                return CODE_BOOLEAN_TRUE;
            }
            return CODE_BOOLEAN_FALSE;
        } else if (o instanceof byte[]) {
            return CODE_BYTEARRAY;
        }
        if (o instanceof DataType) {
            if (o instanceof IntStringValue) {
                return CODE_INT_STRING;
            } else if (o instanceof IntStringStringValue) {
                return CODE_INT_STRING_STRING;
            } else if (o instanceof StringStringValue) {
                return CODE_STRING_STRING;
            } else if (o instanceof LongIntIntByteByteStringValue) {
                return CODE_LONG_INT_INT_BYTE_BYTE_STRING;
            } else if (o instanceof IntBooleanIntBooleanValue) {
                return CODE_INT_BOOLEAN_INT_BOOLEAN;
            } else if (o instanceof BytesStringStringValue) {
                return CODE_BYTES_STRING_STRING;
            }
        }
        return CODE_TOSTRING;
    }

    /**
     * Per-type encoder resolved by a single type dispatch — writes the wire
     * type code, the length prefix and the encoded value into the buffer, so
     * callers do not have to look up the type twice
     * ({@link #getTypeCode(Object)} then {@link #encode(Object, int)}) nor
     * know the wire layout.
     */
    public interface ValueEncoder {
        void encode(Buffer buffer, Object value);
    }

    private static abstract class AbstractValueEncoder implements ValueEncoder {
        private final byte typeCode;

        private AbstractValueEncoder(byte typeCode) {
            this.typeCode = typeCode;
        }

        @Override
        public final void encode(Buffer buffer, Object value) {
            buffer.putByte(typeCode);
            encodeValue(buffer, value);
        }

        /**
         * Writes the length-prefixed value; must produce the same bytes as
         * {@code buffer.putPrefixedBytes(encode(value, typeCode))}.
         */
        abstract void encodeValue(Buffer buffer, Object value);
    }

    private static void putPrefixedSVar32(Buffer buffer, int value) {
        buffer.putSVInt(BytesUtils.computeSVar32Size(value));
        buffer.putSVInt(value);
    }

    /**
     * Size of {@code putPrefixedBytes(bytes)}: {@code putSVInt(NULL)} for null
     * is a single byte, otherwise the svarint length prefix plus the payload.
     */
    private static int prefixedBytesSize(byte[] bytes) {
        if (bytes == null) {
            return 1;
        }
        return BytesUtils.computeSVar32Size(bytes.length) + bytes.length;
    }

    /**
     * Size of {@code putVInt(value)}: a negative int is written as a
     * sign-extended var64 (10 bytes).
     */
    private static int vIntSize(int value) {
        if (value >= 0) {
            return BytesUtils.computeVar32Size(value);
        }
        return BytesUtils.computeVar64Size(value);
    }

    private static final ValueEncoder STRING_ENCODER = new AbstractValueEncoder(CODE_STRING) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            buffer.putPrefixedString((String) value);
        }
    };
    private static final ValueEncoder NULL_ENCODER = new AbstractValueEncoder(CODE_NULL) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            buffer.putPrefixedBytes(null);
        }
    };
    private static final ValueEncoder INT_ENCODER = new AbstractValueEncoder(CODE_INT) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            putPrefixedSVar32(buffer, (Integer) value);
        }
    };
    private static final ValueEncoder LONG_ENCODER = new AbstractValueEncoder(CODE_LONG) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            // zigzag once: putVLong(zigZag) writes the same bytes as putSVLong(value)
            final long zigZagValue = BytesUtils.longToZigZag((Long) value);
            buffer.putSVInt(BytesUtils.computeVar64Size(zigZagValue));
            buffer.putVLong(zigZagValue);
        }
    };
    private static final ValueEncoder BOOLEAN_TRUE_ENCODER = new AbstractValueEncoder(CODE_BOOLEAN_TRUE) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            buffer.putPrefixedBytes(EMPTY_BYTE_ARRAY);
        }
    };
    private static final ValueEncoder BOOLEAN_FALSE_ENCODER = new AbstractValueEncoder(CODE_BOOLEAN_FALSE) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            buffer.putPrefixedBytes(EMPTY_BYTE_ARRAY);
        }
    };
    private static final ValueEncoder BYTEARRAY_ENCODER = new AbstractValueEncoder(CODE_BYTEARRAY) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            buffer.putPrefixedBytes((byte[]) value);
        }
    };
    private static final ValueEncoder BYTE_ENCODER = new AbstractValueEncoder(CODE_BYTE) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            buffer.putSVInt(1);
            buffer.putByte((Byte) value);
        }
    };
    private static final ValueEncoder SHORT_ENCODER = new AbstractValueEncoder(CODE_SHORT) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            putPrefixedSVar32(buffer, (Short) value);
        }
    };
    private static final ValueEncoder FLOAT_ENCODER = new AbstractValueEncoder(CODE_FLOAT) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            buffer.putSVInt(BytesUtils.INT_BYTE_LENGTH);
            buffer.putInt(Float.floatToRawIntBits((Float) value));
        }
    };
    private static final ValueEncoder DOUBLE_ENCODER = new AbstractValueEncoder(CODE_DOUBLE) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            buffer.putSVInt(BytesUtils.LONG_BYTE_LENGTH);
            buffer.putLong(Double.doubleToRawLongBits((Double) value));
        }
    };
    private static final ValueEncoder TOSTRING_ENCODER = new AbstractValueEncoder(CODE_TOSTRING) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            buffer.putPrefixedString(value.toString());
        }
    };
    private static final ValueEncoder INT_STRING_ENCODER = new AbstractValueEncoder(CODE_INT_STRING) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            final IntStringValue intString = (IntStringValue) value;
            final byte[] stringValue = BytesUtils.toBytes(intString.getStringValue());

            buffer.putSVInt(BytesUtils.computeSVar32Size(intString.getIntValue())
                    + prefixedBytesSize(stringValue));
            buffer.putSVInt(intString.getIntValue());
            buffer.putPrefixedBytes(stringValue);
        }
    };
    private static final ValueEncoder INT_STRING_STRING_ENCODER = new AbstractValueEncoder(CODE_INT_STRING_STRING) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            final IntStringStringValue intStringString = (IntStringStringValue) value;
            final byte[] stringValue1 = BytesUtils.toBytes(intStringString.getStringValue1());
            final byte[] stringValue2 = BytesUtils.toBytes(intStringString.getStringValue2());

            buffer.putSVInt(BytesUtils.computeSVar32Size(intStringString.getIntValue())
                    + prefixedBytesSize(stringValue1) + prefixedBytesSize(stringValue2));
            buffer.putSVInt(intStringString.getIntValue());
            buffer.putPrefixedBytes(stringValue1);
            buffer.putPrefixedBytes(stringValue2);
        }
    };
    private static final ValueEncoder STRING_STRING_ENCODER = new AbstractValueEncoder(CODE_STRING_STRING) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            final StringStringValue stringString = (StringStringValue) value;
            final byte[] stringValue1 = BytesUtils.toBytes(stringString.getStringValue1());
            final byte[] stringValue2 = BytesUtils.toBytes(stringString.getStringValue2());

            buffer.putSVInt(prefixedBytesSize(stringValue1) + prefixedBytesSize(stringValue2));
            buffer.putPrefixedBytes(stringValue1);
            buffer.putPrefixedBytes(stringValue2);
        }
    };
    private static final ValueEncoder LONG_INT_INT_BYTE_BYTE_STRING_ENCODER = new AbstractValueEncoder(CODE_LONG_INT_INT_BYTE_BYTE_STRING) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            final LongIntIntByteByteStringValue longValue = (LongIntIntByteByteStringValue) value;
            final byte[] stringValue = BytesUtils.toBytes(longValue.getStringValue());

            int length = 1 + BytesUtils.computeVar64Size(longValue.getLongValue())
                    + vIntSize(longValue.getIntValue1());
            if (isSetIntValue2(longValue)) {
                length += vIntSize(longValue.getIntValue2());
            }
            if (isSetByteValue1(longValue)) {
                length += 1;
            }
            if (isSetByteValue2(longValue)) {
                length += 1;
            }
            if (isSetStringValue(longValue)) {
                length += prefixedBytesSize(stringValue);
            }
            buffer.putSVInt(length);

            buffer.putByte(newBitField(longValue));
            buffer.putVLong(longValue.getLongValue());
            buffer.putVInt(longValue.getIntValue1());
            if (isSetIntValue2(longValue)) {
                buffer.putVInt(longValue.getIntValue2());
            }
            if (isSetByteValue1(longValue)) {
                buffer.putByte(longValue.getByteValue1());
            }
            if (isSetByteValue2(longValue)) {
                buffer.putByte(longValue.getByteValue2());
            }
            if (isSetStringValue(longValue)) {
                buffer.putPrefixedBytes(stringValue);
            }
        }
    };
    private static final ValueEncoder INT_BOOLEAN_INT_BOOLEAN_ENCODER = new AbstractValueEncoder(CODE_INT_BOOLEAN_INT_BOOLEAN) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            final IntBooleanIntBooleanValue intBoolean = (IntBooleanIntBooleanValue) value;

            buffer.putSVInt(vIntSize(intBoolean.getIntValue1()) + 1
                    + vIntSize(intBoolean.getIntValue2()) + 1);
            buffer.putVInt(intBoolean.getIntValue1());
            buffer.putBoolean(intBoolean.isBooleanValue1());
            buffer.putVInt(intBoolean.getIntValue2());
            buffer.putBoolean(intBoolean.isBooleanValue2());
        }
    };
    private static final ValueEncoder BYTES_STRING_STRING_ENCODER = new AbstractValueEncoder(CODE_BYTES_STRING_STRING) {
        @Override
        void encodeValue(Buffer buffer, Object value) {
            final BytesStringStringValue bytesStringString = (BytesStringStringValue) value;
            final byte[] bytesValue = bytesStringString.getBytesValue();
            final byte[] stringValue1 = BytesUtils.toBytes(bytesStringString.getStringValue1());
            final byte[] stringValue2 = BytesUtils.toBytes(bytesStringString.getStringValue2());

            buffer.putSVInt(prefixedBytesSize(bytesValue)
                    + prefixedBytesSize(stringValue1) + prefixedBytesSize(stringValue2));
            buffer.putPrefixedBytes(bytesValue);
            buffer.putPrefixedBytes(stringValue1);
            buffer.putPrefixedBytes(stringValue2);
        }
    };

    /**
     * Resolves the {@link ValueEncoder} for the value with a single type
     * dispatch; mirrors {@link #getTypeCode(Object)}.
     */
    public ValueEncoder getEncoder(Object o) {
        if (o == null) {
            return NULL_ENCODER;
        }
        if (o instanceof Number) {
            if (o instanceof Long) {
                return LONG_ENCODER;
            } else if (o instanceof Integer) {
                return INT_ENCODER;
            } else if (o instanceof Short) {
                return SHORT_ENCODER;
            } else if (o instanceof Float) {
                return FLOAT_ENCODER;
            } else if (o instanceof Double) {
                return DOUBLE_ENCODER;
            } else if (o instanceof Byte) {
                return BYTE_ENCODER;
            }
        }
        if (o instanceof String) {
            return STRING_ENCODER;
        } else if (o instanceof Boolean) {
            if (Boolean.TRUE.equals(o)) {
                return BOOLEAN_TRUE_ENCODER;
            }
            return BOOLEAN_FALSE_ENCODER;
        } else if (o instanceof byte[]) {
            return BYTEARRAY_ENCODER;
        }
        if (o instanceof DataType) {
            if (o instanceof IntStringValue) {
                return INT_STRING_ENCODER;
            } else if (o instanceof IntStringStringValue) {
                return INT_STRING_STRING_ENCODER;
            } else if (o instanceof StringStringValue) {
                return STRING_STRING_ENCODER;
            } else if (o instanceof LongIntIntByteByteStringValue) {
                return LONG_INT_INT_BYTE_BYTE_STRING_ENCODER;
            } else if (o instanceof IntBooleanIntBooleanValue) {
                return INT_BOOLEAN_INT_BOOLEAN_ENCODER;
            } else if (o instanceof BytesStringStringValue) {
                return BYTES_STRING_STRING_ENCODER;
            }
        }
        return TOSTRING_ENCODER;
    }

    public AnnotationBo decodeAnnotation(int key, final byte dataType, final byte[] data) {
        return switch (dataType) {
            case CODE_STRING -> new StringAnnotationBo(key, decodeString(data));
            case CODE_BOOLEAN_TRUE -> new BooleanAnnotationBo(key, Boolean.TRUE);
            case CODE_BOOLEAN_FALSE -> new BooleanAnnotationBo(key, Boolean.FALSE);
            case CODE_INT -> new IntAnnotationBo(key, BytesUtils.bytesToSVar32(data, 0));
            case CODE_LONG -> new LongAnnotationBo(key, BytesUtils.bytesToSVar64(data, 0));
            case CODE_BYTE -> new ByteAnnotationBo(key, data[0]);
            case CODE_SHORT -> new ShortAnnotationBo(key, (short) BytesUtils.bytesToSVar32(data, 0));
            case CODE_FLOAT -> new FloatAnnotationBo(key, Float.intBitsToFloat(ByteArrayUtils.bytesToInt(data, 0)));
            case CODE_DOUBLE ->
                    new DoubleAnnotationBo(key, Double.longBitsToDouble(ByteArrayUtils.bytesToLong(data, 0)));
            case CODE_BYTEARRAY -> new BinaryAnnotationBo(key, data);
            case CODE_NULL -> new NullAnnotationBo(key);
            case CODE_TOSTRING -> new StringAnnotationBo(key, decodeString(data));
            case CODE_INT_STRING -> new DataTypeAnnotationBo(key, decodeIntStringValue(data));
            case CODE_INT_STRING_STRING -> new DataTypeAnnotationBo(key, decodeIntStringStringValue(data));
            case CODE_STRING_STRING -> new DataTypeAnnotationBo(key, decodeStringStringValue(data));
            case CODE_LONG_INT_INT_BYTE_BYTE_STRING ->
                    new DataTypeAnnotationBo(key, decodeLongIntIntByteByteStringValue(data));
            case CODE_INT_BOOLEAN_INT_BOOLEAN -> new DataTypeAnnotationBo(key, decodeIntBooleanIntBooleanValue(data));
            case CODE_BYTES_STRING_STRING -> new DataTypeAnnotationBo(key, decodeBytesStringStringValue(data));
            default -> throw new IllegalArgumentException("unsupported DataType:" + dataType);
        };
    }

    /**
     * @deprecated Since 4.0.0. Use {@link #getEncoder(Object)}, which writes
     * the value into a {@code Buffer} without the intermediate byte array.
     */
    @Deprecated
    public byte[] encode(Object o, int typeCode) {
        switch (typeCode) {
            case CODE_STRING:
                return encodeString((String) o);
            case CODE_INT: {
                return BytesUtils.intToSVar32((Integer) o);
            }
            case CODE_BOOLEAN_TRUE: {
                return EMPTY_BYTE_ARRAY;
            }
            case CODE_BOOLEAN_FALSE: {
                return EMPTY_BYTE_ARRAY;
            }
            case CODE_LONG: {
                return BytesUtils.longToSVar64((Long) o);
            }
            case CODE_BYTE: {
                final byte[] bytes = new byte[1];
                bytes[0] = (Byte) o;
                return bytes;
            }
            case CODE_SHORT: {
                return BytesUtils.intToSVar32((Short) o);
            }
            case CODE_FLOAT: {
                final byte[] buffer = new byte[BytesUtils.INT_BYTE_LENGTH];
                ByteArrayUtils.writeInt(Float.floatToRawIntBits((Float) o), buffer, 0);
                return buffer;
            }
            case CODE_DOUBLE: {
                final byte[] buffer = new byte[BytesUtils.LONG_BYTE_LENGTH];
                ByteArrayUtils.writeLong(Double.doubleToRawLongBits((Double) o), buffer, 0);
                return buffer;
            }
            case CODE_BYTEARRAY:
                return (byte[]) o;
            case CODE_NULL:
                return null;
            case CODE_TOSTRING:
                final String str = o.toString();
                return encodeString(str);
            case CODE_INT_STRING:
                return encodeIntStringValue(o);
            case CODE_INT_STRING_STRING:
                return encodeIntStringStringValue(o);
            case CODE_STRING_STRING:
                return encodeStringStringValue(o);
            case CODE_LONG_INT_INT_BYTE_BYTE_STRING:
                return encodeLongIntIntByteByteStringValue(o);
            case CODE_INT_BOOLEAN_INT_BOOLEAN:
                return encodeIntBooleanIntBooleanValue(o);
            case CODE_BYTES_STRING_STRING:
                return encodeBytesStringStringValue(o);
        }
        throw new IllegalArgumentException("unsupported DataType:" + typeCode + " data:" + o);
    }


    private DataType decodeIntStringValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final int intValue = buffer.readSVInt();
        final String stringValue = BytesUtils.toString(buffer.readPrefixedBytes());
        return new IntStringValue(intValue, stringValue);
    }


    private static byte[] encodeIntStringValue(Object value) {
        final IntStringValue tIntStringValue = (IntStringValue) value;
        final int intValue = tIntStringValue.getIntValue();
        final byte[] stringValue = BytesUtils.toBytes(tIntStringValue.getStringValue());
        // TODO increase by a more precise value
        final int bufferSize = getBufferSize(stringValue, 4 + 8);
        final Buffer buffer = new AutomaticBuffer(bufferSize);
        buffer.putSVInt(intValue);
        buffer.putPrefixedBytes(stringValue);
        return buffer.getBuffer();
    }

    private static int getBufferSize(byte[] stringValue, int reserve) {
        if (stringValue == null) {
            return reserve;
        } else {
            return stringValue.length + reserve;
        }
    }

    private DataType decodeIntStringStringValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final int intValue = buffer.readSVInt();
        final String stringValue1 = BytesUtils.toString(buffer.readPrefixedBytes());
        final String stringValue2 = BytesUtils.toString(buffer.readPrefixedBytes());
        return new IntStringStringValue(intValue, stringValue1, stringValue2);
    }

    private DataType decodeBytesStringStringValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final byte[] bytesValue = buffer.readPrefixedBytes();
        final String stringValue1 = BytesUtils.toString(buffer.readPrefixedBytes());
        final String stringValue2 = BytesUtils.toString(buffer.readPrefixedBytes());
        return new BytesStringStringValue(bytesValue, stringValue1, stringValue2);
    }

    private static byte[] encodeIntStringStringValue(Object o) {
        final IntStringStringValue tIntStringStringValue = (IntStringStringValue) o;
        final int intValue = tIntStringStringValue.getIntValue();
        final byte[] stringValue1 = BytesUtils.toBytes(tIntStringStringValue.getStringValue1());
        final byte[] stringValue2 = BytesUtils.toBytes(tIntStringStringValue.getStringValue2());
        // TODO increase by a more precise value
        final int bufferSize = getBufferSize(stringValue1, stringValue2, 4 + 8);
        final Buffer buffer = new AutomaticBuffer(bufferSize);
        buffer.putSVInt(intValue);
        buffer.putPrefixedBytes(stringValue1);
        buffer.putPrefixedBytes(stringValue2);
        return buffer.getBuffer();
    }

    private static byte[] encodeBytesStringStringValue(Object o) {
        final BytesStringStringValue tBytesStringStringValue = (BytesStringStringValue) o;
        final byte[] bytesValue = tBytesStringStringValue.getBytesValue();
        final byte[] stringValue1 = BytesUtils.toBytes(tBytesStringStringValue.getStringValue1());
        final byte[] stringValue2 = BytesUtils.toBytes(tBytesStringStringValue.getStringValue2());
        final int bufferSize = getBufferSize(bytesValue, stringValue1, stringValue2);
        final Buffer buffer = new AutomaticBuffer(bufferSize);
        buffer.putPrefixedBytes(bytesValue);
        buffer.putPrefixedBytes(stringValue1);
        buffer.putPrefixedBytes(stringValue2);
        return buffer.getBuffer();
    }

    private static int getBufferSize(byte[] bytesValue, byte[] stringValue1, byte[] stringValue2) {
        int length = 0;
        if (bytesValue != null) {
            length += bytesValue.length;
        }
        if (stringValue1 != null) {
            length += stringValue1.length;
        }
        if (stringValue2 != null) {
            length += stringValue2.length;
        }
        return length;
    }

    private static int getBufferSize(byte[] stringValue1, byte[] stringValue2, int reserve) {
        int length = 0;
        if (stringValue1 != null) {
            length += stringValue1.length;
        }
        if (stringValue2 != null) {
            length += stringValue2.length;

        }
        return length + reserve;
    }

    private DataType decodeLongIntIntByteByteStringValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final byte bitField = buffer.readByte();
        final long longValue = buffer.readVLong();
        final int intValue1 = buffer.readVInt();

        int intValue2 = -1;
        if (BitFieldUtils.testBit(bitField, 0)) {
            intValue2 = buffer.readVInt();
        }
        byte byteValue1 = -1;
        if (BitFieldUtils.testBit(bitField, 1)) {
            byteValue1 = buffer.readByte();
        }
        byte byteValue2 = -1;
        if (BitFieldUtils.testBit(bitField, 2)) {
            byteValue2 = buffer.readByte();
        }
        String stringValue = null;
        if (BitFieldUtils.testBit(bitField, 3)) {
            stringValue = BytesUtils.toString(buffer.readPrefixedBytes());
        }
        return new LongIntIntByteByteStringValue(longValue, intValue1, intValue2, byteValue1, byteValue2, stringValue);
    }

    private static byte[] encodeLongIntIntByteByteStringValue(Object o) {
        final LongIntIntByteByteStringValue value = (LongIntIntByteByteStringValue) o;
        byte bitField = 0;
        bitField = newBitField(value);
        final byte[] stringValue = BytesUtils.toBytes(value.getStringValue());

        // bitField + long + int + int + byte + byte + string
        final int bufferSize = getBufferSize(stringValue, 1 + 8 + 4 + 4 + 1 + 1);
        final Buffer buffer = new AutomaticBuffer(bufferSize);
        buffer.putByte(bitField);
        buffer.putVLong(value.getLongValue());
        buffer.putVInt(value.getIntValue1());
        if (isSetIntValue2(value)) {
            buffer.putVInt(value.getIntValue2());
        }
        if (isSetByteValue1(value)) {
            buffer.putByte(value.getByteValue1());
        }
        if (isSetByteValue2(value)) {
            buffer.putByte(value.getByteValue2());
        }
        if (isSetStringValue(value)) {
            buffer.putPrefixedBytes(stringValue);
        }
        return buffer.getBuffer();
    }

    private static byte newBitField(LongIntIntByteByteStringValue value) {

        byte bitField = BitFieldUtils.setBit((byte)0, 0, isSetIntValue2(value));
        bitField = BitFieldUtils.setBit(bitField, 1, isSetByteValue1(value));
        bitField = BitFieldUtils.setBit(bitField, 2, isSetByteValue2(value));
        bitField = BitFieldUtils.setBit(bitField, 3, isSetStringValue(value));
        return bitField;
    }

    private static boolean isSetStringValue(LongIntIntByteByteStringValue value) {
        return value.getStringValue() != null;
    }

    private static boolean isSetByteValue2(LongIntIntByteByteStringValue value) {
        return value.getByteValue2() != 0;
    }

    private static boolean isSetByteValue1(LongIntIntByteByteStringValue value) {
        return value.getByteValue1() != 0;
    }

    private static boolean isSetIntValue2(LongIntIntByteByteStringValue value) {
        return value.getIntValue2() != 0;
    }

    private DataType decodeIntBooleanIntBooleanValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final int intValue1 = buffer.readVInt();
        final boolean booleanValue1 = buffer.readBoolean();
        final int intValue2 = buffer.readVInt();
        final boolean booleanValue2 = buffer.readBoolean();

        return new IntBooleanIntBooleanValue(intValue1, booleanValue1, intValue2, booleanValue2);
    }

    private static byte[] encodeIntBooleanIntBooleanValue(Object o) {
        final IntBooleanIntBooleanValue value = (IntBooleanIntBooleanValue) o;

        // int + int
        final Buffer buffer = new AutomaticBuffer(8);
        buffer.putVInt(value.getIntValue1());
        buffer.putBoolean(value.isBooleanValue1());
        buffer.putVInt(value.getIntValue2());
        buffer.putBoolean(value.isBooleanValue2());
        return buffer.getBuffer();
    }

    private DataType decodeStringStringValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final String stringValue1 = BytesUtils.toString(buffer.readPrefixedBytes());
        final String stringValue2 = BytesUtils.toString(buffer.readPrefixedBytes());
        return new StringStringValue(stringValue1, stringValue2);
    }

    private static byte[]  encodeStringStringValue(Object o) {
        final StringStringValue tStringStringValue = (StringStringValue) o;
        final byte[] stringValue1 = BytesUtils.toBytes(tStringStringValue.getStringValue1());
        final byte[] stringValue2 = BytesUtils.toBytes(tStringStringValue.getStringValue2());
        // TODO increase by a more precise value
        final int bufferSize = getBufferSize(stringValue1, stringValue2);
        final Buffer buffer = new AutomaticBuffer(bufferSize);
        buffer.putPrefixedBytes(stringValue1);
        buffer.putPrefixedBytes(stringValue2);
        return buffer.getBuffer();
    }

    private static int getBufferSize(byte[] stringValue1, byte[] stringValue2) {
        int length = 0;
        if (stringValue1 != null) {
            length += stringValue1.length;
        }
        if (stringValue2 != null) {
            length += stringValue2.length;

        }
        return length;
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
        return BytesUtils.toBytes(in);
    }
}
