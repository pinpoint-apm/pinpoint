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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.grpc.trace.PJvmGcType;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.JvmGcType;

/**
 * @author jaehong.kim
 */
public class GrpcJvmGcTypeMessageConverter implements MessageConverter<PJvmGcType> {

    @Override
    public PJvmGcType toMessage(Object message) {
        if (message instanceof JvmGcType) {
            final JvmGcType jvmGcType = (JvmGcType) message;
            return convertJvmGcType(jvmGcType);
        }
        throw new IllegalArgumentException("invalid message type. message=" + message);
    }

    private PJvmGcType convertJvmGcType(final JvmGcType jvmGcType) {
        switch (jvmGcType) {
            case UNKNOWN:
                return PJvmGcType.JVM_GC_TYPE_UNKNOWN;
            case SERIAL:
                return PJvmGcType.JVM_GC_TYPE_SERIAL;
            case PARALLEL:
                return PJvmGcType.JVM_GC_TYPE_PARALLEL;
            case CMS:
                return PJvmGcType.JVM_GC_TYPE_CMS;
            case G1:
                return PJvmGcType.JVM_GC_TYPE_G1;
            default:
                return PJvmGcType.JVM_GC_TYPE_UNKNOWN;
        }
    }

}
