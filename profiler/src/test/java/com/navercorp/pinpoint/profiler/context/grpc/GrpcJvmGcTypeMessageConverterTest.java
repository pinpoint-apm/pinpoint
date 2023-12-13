/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.grpc.trace.PJvmGcType;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.JvmGcType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcJvmGcTypeMessageConverterTest {

    @Test
    public void toMessage() throws Exception {
        GrpcJvmGcTypeMessageConverter converter = new GrpcJvmGcTypeMessageConverter();
        converter.toMessage(JvmGcType.CMS);
        converter.toMessage(JvmGcType.G1);
        converter.toMessage(JvmGcType.ZGC);
        converter.toMessage(JvmGcType.SERIAL);
        converter.toMessage(JvmGcType.PARALLEL);

        assertEquals(PJvmGcType.JVM_GC_TYPE_CMS, converter.toMessage(JvmGcType.CMS));
        assertEquals(PJvmGcType.JVM_GC_TYPE_G1, converter.toMessage(JvmGcType.G1));
        assertEquals(PJvmGcType.JVM_GC_TYPE_ZGC, converter.toMessage(JvmGcType.ZGC));
        assertEquals(PJvmGcType.JVM_GC_TYPE_SERIAL, converter.toMessage(JvmGcType.SERIAL));
        assertEquals(PJvmGcType.JVM_GC_TYPE_PARALLEL, converter.toMessage(JvmGcType.PARALLEL));
    }
}