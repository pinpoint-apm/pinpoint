package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;

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
}
