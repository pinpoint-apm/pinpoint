package com.profiler.common.bo;

import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.util.Buffer;

import java.nio.charset.Charset;

/**
 *
 */
public class AnnotationBo {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final int VERSION_SIZE = 1;
    // version 0 = prefix의 사이즈를 int로
    // version 1 = prefix의 사이즈를 short로
    // version 2 = prefix의 사이즈를 byte 하면 byte eocnding이 좀 줄지 않나?
    private byte version = 0;
    private long spanId;
    private long timestamp;
    private String key;
    private byte[] keyBytes;

    private long duration;
    private int valueType;
    private byte[] value;


    public AnnotationBo() {
    }

    public AnnotationBo(Annotation ano) {
        this.timestamp = ano.getTimestamp();
        this.key = ano.getKey();
        this.duration = ano.getDuration();
        this.valueType = ano.getValueTypeCode();
        this.value = ano.getValue();
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
            keyBytes = this.key.getBytes(UTF8);
        }
        return keyBytes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getValueType() {
        return valueType;
    }

    public void setValueType(int valueType) {
        this.valueType = valueType;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }


    public int writeValue(byte[] buf, int offset) {
//        long timestamp; // required 8
//        long duration; // optional 8
//        String key; // required 4+string.length
//        int valueTypeCode; // required 4
//        ByteBuffer value; // optional 4 + buf.length
        Buffer buffer = new Buffer(buf, offset);
        buffer.put(this.version);
        buffer.put(this.timestamp);
        buffer.put(this.duration);
        buffer.putPrefixedBytes(getKeyBytes());
        buffer.put(this.valueType);
        buffer.putPrefixedBytes(value);
        return buffer.getOffset();
    }

    public int getBufferSize() {
//        long timestamp; // required 8
//        long duration; // optional 8
//        String key; // required 4+string.length
//        int valueTypeCode; // required 4
//        ByteBuffer value; // optional 4 + buf.length
        int size = 0;
        size += 1 + 8 + 8 + 4 + 4 + 4;
        size += this.getKeyBytes().length;
        if (this.getValue() != null) {
            size += this.getValue().length;
        }
        return size;
    }


    public int readValue(byte[] buf, int offset) {
        Buffer buffer = new Buffer(buf, offset);
        this.version = buffer.readByte();
        this.timestamp = buffer.readLong();
        this.duration = buffer.readLong();
        this.key = buffer.readPrefixedString();
        this.valueType = buffer.readInt();
        this.value = buffer.readPrefixedBytes();
        return buffer.getOffset();
    }

    @Override
    public String toString() {
        return "AnnotationBo{" +
                "version=" + version +
                ", spanId=" + spanId +
                ", timestamp=" + timestamp +
                ", key='" + key + '\'' +
                ", keyBytes=" + keyBytes +
                ", duration=" + duration +
                ", valueType=" + valueType +
                ", value=" + value +
                '}';
    }
}
