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

package com.navercorp.pinpoint.common.util;

/**
 * @author HyunGil Jeong
 */
public enum JvmType {
    UNKNOWN(null),
    IBM("IBM"),
    OPENJDK("OpenJDK"),
    ORACLE("HotSpot");

    private final String inclusiveString;

    JvmType(String inclusiveString) {
        this.inclusiveString = inclusiveString;
    }

    public static JvmType fromVendor(String vendorName) {
        if (vendorName == null) {
            return UNKNOWN;
        }
        final String vendorNameTrimmed = vendorName.trim();
        for (JvmType jvmType : JvmType.values()) {
            if (jvmType.toString().equalsIgnoreCase(vendorNameTrimmed)) {
                return jvmType;
            }
        }
        return UNKNOWN;
    }

    public static JvmType fromVmName(String vmName) {
        if (vmName == null) {
            return UNKNOWN;
        }
        for (JvmType jvmType : JvmType.values()) {
            if (jvmType.inclusiveString == null) {
                continue;
            } else if (vmName.contains(jvmType.inclusiveString)) {
                return jvmType;
            }
        }
        return UNKNOWN;
    }
}
