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

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.profiler.monitor.metric.gc.JvmGcType;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;

/**
 * @author jaehong.kim
 */
public class JvmGcTypeThriftMessageConverter implements MessageConverter<TJvmGcType> {

    @Override
    public TJvmGcType toMessage(Object message) {
        if (message instanceof JvmGcType) {
            final JvmGcType jvmGcType = (JvmGcType) message;
            return convertJvmGcType(jvmGcType);
        }
        throw new IllegalArgumentException("invalid message type. message=" + message);
    }

    private TJvmGcType convertJvmGcType(final JvmGcType jvmGcType) {
        switch (jvmGcType) {
            case UNKNOWN:
                return TJvmGcType.UNKNOWN;
            case SERIAL:
                return TJvmGcType.SERIAL;
            case PARALLEL:
                return TJvmGcType.PARALLEL;
            case CMS:
                return TJvmGcType.CMS;
            case G1:
                return TJvmGcType.G1;
            default:
                return TJvmGcType.UNKNOWN;
        }
    }
}
