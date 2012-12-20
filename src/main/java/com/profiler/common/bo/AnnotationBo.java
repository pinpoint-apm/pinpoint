package com.profiler.common.bo;

import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.util.AnnotationTranscoder;
import com.profiler.common.util.Buffer;
import com.profiler.common.util.BytesUtils;

/**
 *
 */
public class AnnotationBo implements Comparable<String> {

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

    private static final int VERSION_SIZE = 1;
    // version 0 = prefix의 사이즈를 int로
    // version 1 = prefix의 사이즈를 short로
    // version 2 = prefix의 사이즈를 byte 하면 byte eocnding이 좀 줄지 않나?
    private byte version = 0;
    private long spanId;

    private String key;
    private byte[] keyBytes;

    private int valueType;
    private byte[] byteValue;
    private Object value;

    public AnnotationBo() {
    }

    public AnnotationBo(Annotation ano) {
        this.key = ano.getKey();
        this.valueType = ano.getValueTypeCode();
        this.byteValue = ano.getValue();
    }

    public long getSpanId() {
        return spanId;
    }

    public void setSpanId(long spanId) {
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getKeyBytes() {
        if (keyBytes == null) {
            keyBytes = BytesUtils.getBytes(key);
        }
        return keyBytes;
    }


    public int getValueType() {
        return valueType;
    }

    public void setValueType(int valueType) {
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
        // String key; // required 4+string.length
        // int valueTypeCode; // required 4
        // ByteBuffer value; // optional 4 + buf.length
        buffer.put(this.version);
        buffer.putPrefixedBytes(getKeyBytes());
        buffer.put(this.valueType);
        buffer.putPrefixedBytes(this.byteValue);
    }

    public int getBufferSize() {
        // String key; // required 4+string.length
        // int valueTypeCode; // required 4
        // ByteBuffer value; // optional 4 + buf.length
        int size = 0;
        size += 1 + 4 + 4 + 4;
        size += this.getKeyBytes().length;
        if (this.getByteValue() != null) {
            size += this.getByteValue().length;
        }
        return size;
    }

    public int readValue(byte[] buf, int offset) {
        Buffer buffer = new Buffer(buf, offset);
        readValue(buffer);
        return buffer.getOffset();
    }

    public void readValue(Buffer buffer) {
        this.version = buffer.readByte();
        this.key = buffer.readPrefixedString();
        this.valueType = buffer.readInt();
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

    @Override
    public int compareTo(String key) {
        return this.key.compareTo(key);
    }
}
