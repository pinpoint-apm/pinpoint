package com.profiler.common.bo;

import com.profiler.common.AnnotationKey;
import com.profiler.common.dto2.thrift.Annotation;
import com.profiler.common.util.AnnotationTranscoder;
import com.profiler.common.buffer.Buffer;
import com.profiler.common.buffer.FixedBuffer;

import java.util.Comparator;

/**
 *
 */
public class AnnotationBo {

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

    private static final int VERSION_SIZE = 1;

    private byte version = 0;
    private int spanId;

    private int key;

    private byte valueType;
    private byte[] byteValue;
    private Object value;

    public AnnotationBo() {
    }

    public AnnotationBo(Annotation ano) {
        this.key = ano.getKey();
        Object value = transcoder.getMappingValue(ano);
        this.valueType = transcoder.getTypeCode(value);
        this.byteValue = transcoder.encode(value, this.valueType);
    }

    public int getSpanId() {
        return spanId;
    }

    public void setSpanId(int spanId) {
        this.spanId = spanId;
    }

    public int getVersion() {
        return version & 0xFF;
    }

    public void setVersion(int version) {
        if (version < 0 || version > 255) {
            throw new IllegalArgumentException("out of range (0~255)");
        }
        // range 체크
        this.version = (byte) (version & 0xFF);
    }

    public int getKey() {
        return key;
    }

    public String getKeyName() {
        return AnnotationKey.findAnnotationKey(this.key).getValue();
    }

    public void setKey(int key) {
        this.key = key;
    }


    public int getValueType() {
        return valueType;
    }

    public void setValueType(byte valueType) {
        this.valueType = valueType;
    }

    public byte[] getByteValue() {
        return byteValue;
    }

    public void setByteValue(byte[] byteValue) {
        this.byteValue = byteValue;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void writeValue(Buffer buffer) {
        // long timestamp; // required 8
        // long duration; // optional 8
        // int key; // required 4
        // int valueTypeCode; // required 4
        // ByteBuffer value; // optional 4 + buf.length
        buffer.put(this.version);
        buffer.putSVar(this.key);
        buffer.put(this.valueType);
        buffer.putPrefixedBytes(this.byteValue);
    }

//    public int getBufferSize() {
//        // int key; // required 4+string.length
//        // int valueTypeCode; // required 4
//        // ByteBuffer value; // optional 4 + buf.length
//        int size = 0;
//        size += 1 + 4 + 4 + 4;
//        size += 4;
//        if (this.getByteValue() != null) {
//            size += this.getByteValue().length;
//        }
//        return size;
//    }


    public void readValue(Buffer buffer) {
        this.version = buffer.readByte();
        this.key = buffer.readSVarInt();
        this.valueType = buffer.readByte();
        this.byteValue = buffer.readPrefixedBytes();
        this.value = transcoder.decode(valueType, byteValue);
    }

    @Override
    public String toString() {
        if (value == null) {
            return "AnnotationBo{" + "version=" + version + ", spanId=" + spanId + ", key='" + key + '\'' + ", valueType=" + valueType + ", byteValue=" + byteValue + '}';
        }
        return "AnnotationBo{" + "version=" + version + ", spanId=" + spanId + ", key='" + key + '\'' + ", value=" + value + '}';
    }

}
