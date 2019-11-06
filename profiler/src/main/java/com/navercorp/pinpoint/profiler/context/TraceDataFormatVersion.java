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

import com.navercorp.pinpoint.io.SpanVersion;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum TraceDataFormatVersion {

    V1(SpanVersion.TRACE_V1),
    V2(SpanVersion.TRACE_V2);

    public static final String THRIFT_TRACE_VERSION_KEY = "profiler.transport.thrift.trace.dataformat.version";

    public static final String GRPC_TRACE_VERSION_KEY = "profiler.transport.grpc.trace.dataformat.version";

    private byte version;

    TraceDataFormatVersion(byte version) {
        this.version = version;
    }

    public byte getVersion() {
        return version;
    }
}
