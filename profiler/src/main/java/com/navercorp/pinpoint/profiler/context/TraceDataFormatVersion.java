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

    private static final String THRIFT_TRACE_VERSION_KEY = "profiler.transport.thrift.trace.dataformat.version";

    private static final String GRPC_TRACE_VERSION_KEY = "profiler.transport.grpc.trace.dataformat.version";

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
        final String transportModule = profilerConfig.getTransportModule();
        if ("THRIFT".equalsIgnoreCase(transportModule)) {
            final String version = profilerConfig.readString(THRIFT_TRACE_VERSION_KEY, "v1");
            if ("v1".equalsIgnoreCase(version)) {
                return V1;
            }
            throw new UnsupportedOperationException("unknown " + THRIFT_TRACE_VERSION_KEY + ":" + version);
        }


        if ("GRPC".equalsIgnoreCase(transportModule)) {
            final String version = profilerConfig.readString(GRPC_TRACE_VERSION_KEY, "v2");
            if ("v2".equalsIgnoreCase(version)) {
                return V2;
            }
            throw new UnsupportedOperationException("unknown " + GRPC_TRACE_VERSION_KEY + ":" + version);
        }

        throw new UnsupportedOperationException("unknown transportModule:" + transportModule);
    }
}
