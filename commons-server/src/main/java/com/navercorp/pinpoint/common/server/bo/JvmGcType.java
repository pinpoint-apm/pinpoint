/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public enum JvmGcType {
    UNKNOWN(0),
    SERIAL(1),
    PARALLEL(2),
    CMS(3),
    G1(4);

    private final int typeCode;

    private static final Set<JvmGcType> JVM_GC_TYPES = EnumSet.allOf(JvmGcType.class);


    JvmGcType(int typeCode) {
        this.typeCode = typeCode;
    }

    public int getTypeCode() {
        return this.typeCode;
    }

    public static JvmGcType getTypeByCode(int typeCode) {
        for (JvmGcType gcType : JVM_GC_TYPES) {
            if (gcType.typeCode == typeCode) {
                return gcType;
            }
        }
        return JvmGcType.UNKNOWN;
    }
}
