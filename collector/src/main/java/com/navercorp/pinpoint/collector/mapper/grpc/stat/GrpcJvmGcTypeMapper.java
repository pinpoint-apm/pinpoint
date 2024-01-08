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
import com.navercorp.pinpoint.grpc.trace.PJvmGcType;
import org.springframework.stereotype.Component;

/**
 * @author jaehong.kim
 */
@Component
public class GrpcJvmGcTypeMapper {

    public JvmGcType map(final PJvmGcType type) {
        return switch (type) {
            case JVM_GC_TYPE_UNKNOWN -> JvmGcType.UNKNOWN;
            case JVM_GC_TYPE_SERIAL -> JvmGcType.SERIAL;
            case JVM_GC_TYPE_PARALLEL -> JvmGcType.PARALLEL;
            case JVM_GC_TYPE_CMS -> JvmGcType.CMS;
            case JVM_GC_TYPE_G1 -> JvmGcType.G1;
            case JVM_GC_TYPE_ZGC -> JvmGcType.ZGC;
            default -> JvmGcType.UNKNOWN;
        };
    }
}