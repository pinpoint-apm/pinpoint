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

package com.navercorp.pinpoint.profiler.context.provider.thrift;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessorV1;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanProcessorProvider implements Provider<SpanProcessor<TSpan, TSpanChunk>> {

    private final TraceDataFormatVersion version;

    @Inject
    public SpanProcessorProvider(TraceDataFormatVersion version) {
        this.version = Assert.requireNonNull(version, "version");
    }

    @Override
    public SpanProcessor<TSpan, TSpanChunk> get() {
        if (version == TraceDataFormatVersion.V1) {
            return new SpanProcessorV1();
        }
        throw new UnsupportedOperationException("unknown version :" + version);
    }

}
