/*
 * Copyright 2018 Naver Corp.
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

import java.util.EnumSet;

/**
 * @author Roy Kim
 */
public enum OsType {
    UNKNOWN(null),
    WINDOW("Window"),
    MAC("Mac"),
    LINUX("Linux"),
    SOLARIS("Solaris"),
    AIX("AIX"),
    HP_UX("HP-UX"),
    BSD("BSD");

    public String getInclusiveString() {
        return inclusiveString;
    }

    private final String inclusiveString;

    private static EnumSet<OsType> OS_TYPE = EnumSet.allOf(OsType.class);

    OsType(String inclusiveString) {
        this.inclusiveString = inclusiveString;
    }

    public static OsType fromVendor(String vendorName) {
        if (vendorName == null) {
            return UNKNOWN;
        }
        final String vendorNameTrimmed = vendorName.trim();
        for (OsType osType : OS_TYPE) {
            if (osType.toString().equalsIgnoreCase(vendorNameTrimmed)) {
                return osType;
            }
        }
        return UNKNOWN;
    }

    public static OsType fromOsName(String osName) {
        if (osName == null) {
            return UNKNOWN;
        }
        for (OsType osType : OS_TYPE) {
            if (osType.inclusiveString == null) {
                continue;
            }
            if (osName.toLowerCase().contains(osType.inclusiveString.toLowerCase())) {
                return osType;
            }
        }
        return UNKNOWN;
    }
}
