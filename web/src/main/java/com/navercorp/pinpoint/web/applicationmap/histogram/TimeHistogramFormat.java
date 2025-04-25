/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.histogram;

import java.util.EnumSet;
import java.util.Set;

public enum TimeHistogramFormat {
    @Deprecated
    V1, // key is slot("1s", "3s", "5s", "Slow", "Error"), value is {timestamp : count}
    V2, // key is timestamp, value is [1s, 3s, 5s, Slow, Error, Avg, Max, Sum, Tot] - LoadHistogram
    V3; // key is slot("1s", "3s", "5s", "Slow", "Error"), value is {count}, timestamp is in root

    private static final Set<TimeHistogramFormat> VERSIONS = EnumSet.allOf(TimeHistogramFormat.class);

    public static TimeHistogramFormat format(boolean useLoadHistogramFormat) {
        if (useLoadHistogramFormat) {
            return V2;
        }
        return V1;
    }

    public static TimeHistogramFormat format(int version) {
        return format(version, null);
    }

    public static TimeHistogramFormat format(int version, TimeHistogramFormat defaultVersion) {
        if (version == 3) {
            return V3;
        } else if (version == 2) {
            return V2;
        } else if (version == 1) {
            return V1;
        }
        return defaultVersion;
    }

    public static TimeHistogramFormat format(String version) {
        return format(version, null);
    }

    public static TimeHistogramFormat format(String format, TimeHistogramFormat defaultVersion) {
        for (TimeHistogramFormat version : VERSIONS) {
            if (version.name().equalsIgnoreCase(format)) {
                return version;
            }
        }
        return defaultVersion;
    }
}