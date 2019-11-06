/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.grpc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.compress.GrpcSpanProcessorV2;

public class GrpcSpanProcessorProvider implements Provider<SpanProcessor<PSpan.Builder, PSpanChunk.Builder>> {

    private final TraceDataFormatVersion version;

    @Inject
    public GrpcSpanProcessorProvider(TraceDataFormatVersion version) {
        this.version = Assert.requireNonNull(version, "version");
    }

    @Override
    public SpanProcessor<PSpan.Builder, PSpanChunk.Builder> get() {
        if (version == TraceDataFormatVersion.V2) {
            return new GrpcSpanProcessorV2();
        }
        throw new UnsupportedOperationException("unknown version :" + version);
    }

}
