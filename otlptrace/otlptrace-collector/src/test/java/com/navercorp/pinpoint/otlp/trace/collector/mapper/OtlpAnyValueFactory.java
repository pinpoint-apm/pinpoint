package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.common.v1.KeyValueList;

public final class OtlpAnyValueFactory {

    private OtlpAnyValueFactory() {
    }

    public static KeyValue kv(String key, AnyValue value) {
        return KeyValue.newBuilder().setKey(key).setValue(value).build();
    }

    public static AnyValue strVal(String s) {
        return AnyValue.newBuilder().setStringValue(s).build();
    }

    public static AnyValue intVal(long v) {
        return AnyValue.newBuilder().setIntValue(v).build();
    }

    public static AnyValue boolVal(boolean v) {
        return AnyValue.newBuilder().setBoolValue(v).build();
    }

    public static AnyValue doubleVal(double v) {
        return AnyValue.newBuilder().setDoubleValue(v).build();
    }

    public static AnyValue bytesVal(byte[] bytes) {
        return AnyValue.newBuilder().setBytesValue(ByteString.copyFrom(bytes)).build();
    }

    public static AnyValue arrayVal(AnyValue... items) {
        ArrayValue.Builder builder = ArrayValue.newBuilder();
        for (AnyValue item : items) {
            builder.addValues(item);
        }
        return AnyValue.newBuilder().setArrayValue(builder.build()).build();
    }

    public static AnyValue kvlistVal(KeyValue... entries) {
        KeyValueList.Builder builder = KeyValueList.newBuilder();
        for (KeyValue kv : entries) {
            builder.addValues(kv);
        }
        return AnyValue.newBuilder().setKvlistValue(builder.build()).build();
    }
}
