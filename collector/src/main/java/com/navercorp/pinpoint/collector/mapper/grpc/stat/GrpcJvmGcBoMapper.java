/*
 * Copyright 2019 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.grpc.trace.PJvmGc;
import com.navercorp.pinpoint.grpc.trace.PJvmGcType;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class GrpcJvmGcBoMapper {

    public JvmGcBo map(final PJvmGc jvmGc) {
        final JvmGcBo jvmGcBo = new JvmGcBo();
        jvmGcBo.setGcType(toJvmGcType(jvmGc.getType()));
        jvmGcBo.setHeapUsed(jvmGc.getJvmMemoryHeapUsed());
        jvmGcBo.setHeapMax(jvmGc.getJvmMemoryHeapMax());
        jvmGcBo.setNonHeapUsed(jvmGc.getJvmMemoryNonHeapUsed());
        jvmGcBo.setNonHeapMax(jvmGc.getJvmMemoryNonHeapMax());
        jvmGcBo.setGcOldCount(jvmGc.getJvmGcOldCount());
        jvmGcBo.setGcOldTime(jvmGc.getJvmGcOldTime());
        return jvmGcBo;
    }

    private JvmGcType toJvmGcType(final PJvmGcType type) {
        switch (type) {
            case JVM_GC_TYPE_UNKNOWN:
                return JvmGcType.UNKNOWN;
            case JVM_GC_TYPE_SERIAL:
                return JvmGcType.SERIAL;
            case JVM_GC_TYPE_PARALLEL:
                return JvmGcType.PARALLEL;
            case JVM_GC_TYPE_CMS:
                return JvmGcType.CMS;
            case JVM_GC_TYPE_G1:
                return JvmGcType.G1;
            default:
                return JvmGcType.UNKNOWN;
        }
    }
}

