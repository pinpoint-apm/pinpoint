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

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.grpc.trace.PJvmGcType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcJvmGcTypeMapperTest {

    @Test
    public void map() throws Exception {
        GrpcJvmGcTypeMapper mapper = new GrpcJvmGcTypeMapper();
        JvmGcType jvmGcType = mapper.map(PJvmGcType.JVM_GC_TYPE_CMS);
        assertEquals(jvmGcType, JvmGcType.CMS);
        jvmGcType = mapper.map(PJvmGcType.JVM_GC_TYPE_G1);
        assertEquals(jvmGcType, JvmGcType.G1);
        jvmGcType = mapper.map(PJvmGcType.JVM_GC_TYPE_SERIAL);
        assertEquals(jvmGcType, JvmGcType.SERIAL);
        jvmGcType = mapper.map(PJvmGcType.JVM_GC_TYPE_PARALLEL);
        assertEquals(jvmGcType, JvmGcType.PARALLEL);
        jvmGcType = mapper.map(PJvmGcType.JVM_GC_TYPE_ZGC);
        assertEquals(jvmGcType, JvmGcType.ZGC);
    }
}