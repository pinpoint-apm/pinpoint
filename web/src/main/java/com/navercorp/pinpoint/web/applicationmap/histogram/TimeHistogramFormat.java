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

public enum TimeHistogramFormat {
    @Deprecated
    V1, // key is slot("1s", "3s", "5s", "Slow", "Error"), value is {timestamp : count}
    V2, // key is timestamp, value is [1s, 3s, 5s, Slow, Error, Avg, Max, Sum, Tot] - LoadHistogram
    V3;


    public static TimeHistogramFormat format(boolean useLoadHistogramFormat) {
        if (useLoadHistogramFormat) {
            return V2;
        }
        return V1;
    }

    public static TimeHistogramFormat format(int version) {
        if (version == 3) {
            return V3;
        } else if (version == 2) {
            return V2;
        } else if (version == 1) {
            return V1;
        }
        return V2;
    }
}