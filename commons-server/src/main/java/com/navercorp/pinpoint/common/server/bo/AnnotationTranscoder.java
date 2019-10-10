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
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.profiler.encoding.BitFieldUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
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

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];


    public Object decode(final byte dataType, final byte[] data) {
        switch (dataType) {
            case CODE_STRING:
                return decodeString(data);
            case CODE_BOOLEAN_TRUE:
                return Boolean.TRUE;
            case CODE_BOOLEAN_FALSE:
                return Boolean.FALSE;
            case CODE_INT: {
                return BytesUtils.bytesToSVar32(data, 0);
            }
            case CODE_LONG: {
                return BytesUtils.bytesToSVar64(data, 0);
            }
            case CODE_BYTE:
                return data[0];
            case CODE_SHORT:
                // need short casting
                return (short) BytesUtils.bytesToSVar32(data, 0);
            case CODE_FLOAT:
                return Float.intBitsToFloat(BytesUtils.bytesToInt(data, 0));
            case CODE_DOUBLE:
                return Double.longBitsToDouble(BytesUtils.bytesToLong(data, 0));
            case CODE_BYTEARRAY:
                return data;
            case CODE_NULL:
                return null;
            case CODE_TOSTRING:
                return decodeString(data);
            case CODE_INT_STRING:
                return decodeIntStringValue(data);
            case CODE_INT_STRING_STRING:
                return decodeIntStringStringValue(data);
            case CODE_STRING_STRING:
                return decodeStringStringValue(data);
            case CODE_LONG_INT_INT_BYTE_BYTE_STRING:
                return decodeLongIntIntByteByteStringValue(data);
            case CODE_INT_BOOLEAN_INT_BOOLEAN:
                return decodeIntBooleanIntBooleanValue(data);
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
            // not supported by thrift
            return CODE_FLOAT;
        } else if (o instanceof Double) {
            return CODE_DOUBLE;
        } else if (o instanceof byte[]) {
            return CODE_BYTEARRAY;
        } else if (o instanceof IntStringValue) {
            return CODE_INT_STRING;
        } else if (o instanceof IntStringStringValue) {
            return CODE_INT_STRING_STRING;
        } else if (o instanceof StringStringValue) {
            return CODE_STRING_STRING;
        } else if (o instanceof LongIntIntByteByteStringValue) {
            return CODE_LONG_INT_INT_BYTE_BYTE_STRING;
        } else if (o instanceof IntBooleanIntBooleanValue) {
            return CODE_INT_BOOLEAN_INT_BOOLEAN;
        }
        return CODE_TOSTRING;
    }

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
                BytesUtils.writeInt(Float.floatToRawIntBits((Float) o), buffer, 0);
                return buffer;
            }
            case CODE_DOUBLE: {
                final byte[] buffer = new byte[BytesUtils.LONG_BYTE_LENGTH];
                BytesUtils.writeLong(Double.doubleToRawLongBits((Double) o), buffer, 0);
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
        }
        throw new IllegalArgumentException("unsupported DataType:" + typeCode + " data:" + o);
    }


    private Object decodeIntStringValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final int intValue = buffer.readSVInt();
        final String stringValue = BytesUtils.toString(buffer.readPrefixedBytes());
        return new IntStringValue(intValue, stringValue);
    }


    private byte[] encodeIntStringValue(Object value) {
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

    private int getBufferSize(byte[] stringValue, int reserve) {
        if (stringValue == null) {
            return reserve;
        } else {
            return stringValue.length + reserve;
        }
    }

    private Object decodeIntStringStringValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final int intValue = buffer.readSVInt();
        final String stringValue1 = BytesUtils.toString(buffer.readPrefixedBytes());
        final String stringValue2 = BytesUtils.toString(buffer.readPrefixedBytes());
        return new IntStringStringValue(intValue, stringValue1, stringValue2);
    }

    private byte[] encodeIntStringStringValue(Object o) {
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

    private int getBufferSize(byte[] stringValue1, byte[] stringValue2, int reserve) {
        int length = 0;
        if (stringValue1 != null) {
            length += stringValue1.length;
        }
        if (stringValue2 != null) {
            length += stringValue2.length;

        }
        return length + reserve;
    }

    private Object decodeLongIntIntByteByteStringValue(byte[] data) {
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

    private byte[] encodeLongIntIntByteByteStringValue(Object o) {
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

    private byte newBitField(LongIntIntByteByteStringValue value) {

        byte bitField = BitFieldUtils.setBit((byte)0, 0, isSetIntValue2(value));
        bitField = BitFieldUtils.setBit(bitField, 1, isSetByteValue1(value));
        bitField = BitFieldUtils.setBit(bitField, 2, isSetByteValue2(value));
        bitField = BitFieldUtils.setBit(bitField, 3, isSetStringValue(value));
        return bitField;
    }

    private boolean isSetStringValue(LongIntIntByteByteStringValue value) {
        return value.getStringValue() != null;
    }

    private boolean isSetByteValue2(LongIntIntByteByteStringValue value) {
        return value.getByteValue2() != 0;
    }

    private boolean isSetByteValue1(LongIntIntByteByteStringValue value) {
        return value.getByteValue1() != 0;
    }

    private boolean isSetIntValue2(LongIntIntByteByteStringValue value) {
        return value.getIntValue2() != 0;
    }

    private Object decodeIntBooleanIntBooleanValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final int intValue1 = buffer.readVInt();
        final boolean booleanValue1 = buffer.readBoolean();
        final int intValue2 = buffer.readVInt();
        final boolean booleanValue2 = buffer.readBoolean();

        return new IntBooleanIntBooleanValue(intValue1, booleanValue1, intValue2, booleanValue2);
    }

    private byte[] encodeIntBooleanIntBooleanValue(Object o) {
        final IntBooleanIntBooleanValue value = (IntBooleanIntBooleanValue) o;

        // int + int
        final Buffer buffer = new AutomaticBuffer(8);
        buffer.putVInt(value.getIntValue1());
        buffer.putBoolean(value.isBooleanValue1());
        buffer.putVInt(value.getIntValue2());
        buffer.putBoolean(value.isBooleanValue2());
        return buffer.getBuffer();
    }
//    private Object decodeIntStringStringValue(byte[] data) {
//        final Buffer buffer = new FixedBuffer(data);
//        final int intValue = buffer.readSVInt();
//        final String stringValue1 = BytesUtils.toString(buffer.readPrefixedBytes());
//        final String stringValue2 = BytesUtils.toString(buffer.readPrefixedBytes());
//        return new IntStringStringValue(intValue, stringValue1, stringValue2);
//    }
//
//    private byte[] encodeIntStringStringValue(Object o) {
//        final TIntStringStringValue tIntStringStringValue = (TIntStringStringValue) o;
//        final int intValue = tIntStringStringValue.getIntValue();
//        final byte[] stringValue1 = BytesUtils.toBytes(tIntStringStringValue.getStringValue1());
//        final byte[] stringValue2 = BytesUtils.toBytes(tIntStringStringValue.getStringValue2());
//        // TODO increase by a more precise value
//        final int bufferSize = getBufferSize(stringValue1, stringValue2, 4 + 8);
//        final Buffer buffer = new AutomaticBuffer(bufferSize);
//        buffer.putSVInt(intValue);
//        buffer.putPrefixedBytes(stringValue1);
//        buffer.putPrefixedBytes(stringValue2);
//        return buffer.getBuffer();
//    }
//
//    private int getBufferSize(byte[] stringValue1, byte[] stringValue2, int reserve) {
//        int length = 0;
//        if (stringValue1 != null) {
//            length += stringValue1.length;
//        }
//        if (stringValue2 != null) {
//            length += stringValue2.length;
//
//        }
//        return length + reserve;
//    }
//

    private Object decodeStringStringValue(byte[] data) {
        final Buffer buffer = new FixedBuffer(data);
        final String stringValue1 = BytesUtils.toString(buffer.readPrefixedBytes());
        final String stringValue2 = BytesUtils.toString(buffer.readPrefixedBytes());
        return new StringStringValue(stringValue1, stringValue2);
    }

    private byte[] encodeStringStringValue(Object o) {
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

    private int getBufferSize(byte[] stringValue1, byte[] stringValue2) {
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
