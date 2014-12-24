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

    // 향후 다른 샘플링 스펙이 추가될수 있으므로
    // 일부러 1개 byte를 소비하여 sampling마크 한다.
    public static final String SAMPLING_RATE_PREFIX = "s";


    public static final String SAMPLING_RATE_FALSE = SAMPLING_RATE_PREFIX +  "0";
    public static final String SAMPLING_RATE_TRUE = SAMPLING_RATE_PREFIX +  "1";

    private SamplingFlagUtils() {
    }

    public static boolean isSamplingFlag(String samplingFlag) {
        if (samplingFlag == null) {
            return true;
        }
        // 정확하게 하지 말란 flag가 세팅되었을 경우만 샘플링을 하지 않는다.
        // prefix를 보고 뭔가 더 정확하게 동작되어야 필요성이 있음.
        if (samplingFlag.startsWith(SAMPLING_RATE_PREFIX)) {
            return !SAMPLING_RATE_FALSE.equals(samplingFlag);
        }
        return true;
    }
}

