/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.thrift.dto.TraceConstants;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum TraceDataFormatVersion {

    V1(TraceConstants.TRACE_V1),
    V2(TraceConstants.TRACE_V2);

    private static final String TRACE_VERSION_NAME = "profiler.trace.dataformat.version";

    private byte version;

    TraceDataFormatVersion(byte version) {
        this.version = version;
    }

    public byte getVersion() {
        return version;
    }

    public static TraceDataFormatVersion getTraceDataFormatVersion(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }

        final String lowerCaseVersion = profilerConfig.readString(TRACE_VERSION_NAME, "v1").toLowerCase();
        if ("v2".equals(lowerCaseVersion)) {
            return V2;
        }
        if("v1".equals(lowerCaseVersion)) {
            return V1;
        }
        throw new UnsupportedOperationException("unknown profiler.trace.dataformat.version:" + lowerCaseVersion);
    }
}
