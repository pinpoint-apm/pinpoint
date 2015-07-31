/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.sampler;

/**
 * @author emeroad
 */
public final class SamplingFlagUtils {

    // 1 byte dummy mark for further expansion of sampling specs
    public static final String SAMPLING_RATE_PREFIX = "s";


    public static final String SAMPLING_RATE_FALSE = SAMPLING_RATE_PREFIX +  "0";
    public static final String SAMPLING_RATE_TRUE = SAMPLING_RATE_PREFIX +  "1";

    private SamplingFlagUtils() {
    }

    public static boolean isSamplingFlag(String samplingFlag) {
        if (samplingFlag == null) {
            return true;
        }
        // we turn off sampling only when a specific flag was given
        // XXX needs better detection mechanism through prefix parsing
        if (samplingFlag.startsWith(SAMPLING_RATE_PREFIX)) {
            return !SAMPLING_RATE_FALSE.equals(samplingFlag);
        }
        return true;
    }
}

