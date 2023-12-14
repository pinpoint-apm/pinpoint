package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.grpc.trace.PJvmGcType;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.JvmGcType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author intr3p1d
 */
class GrpcJvmGcTypeMessageConverterTest {

    GrpcJvmGcTypeMessageConverter converter = new GrpcJvmGcTypeMessageConverter();

    @Test
    void testEnumToEnumMap() {
        for (JvmGcType jvmGcType : JvmGcType.values()) {
            PJvmGcType pJvmGcType = converter.convertJvmGcType(jvmGcType);
            assertEquals("JVM_GC_TYPE_" + jvmGcType.name(), pJvmGcType.name());
        }
    }
}