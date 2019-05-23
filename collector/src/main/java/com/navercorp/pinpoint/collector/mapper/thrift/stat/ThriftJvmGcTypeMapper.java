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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;
import org.springframework.stereotype.Component;

/**
 * @author jaehong.kim
 */
@Component
public class ThriftJvmGcTypeMapper {

    public JvmGcType map(final TJvmGcType type) {
        switch (type) {
            case UNKNOWN:
                return JvmGcType.UNKNOWN;
            case SERIAL:
                return JvmGcType.SERIAL;
            case PARALLEL:
                return JvmGcType.PARALLEL;
            case CMS:
                return JvmGcType.CMS;
            case G1:
                return JvmGcType.G1;
            default:
                return JvmGcType.UNKNOWN;
        }
    }
}