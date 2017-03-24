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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.context.SpanPostProcessor;
import com.navercorp.pinpoint.profiler.context.SpanPostProcessorV1;
import com.navercorp.pinpoint.profiler.context.SpanPostProcessorV2;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanPostProcessorProvider implements Provider<SpanPostProcessor> {

    private final TraceDataFormatVersion version;

    @Inject
    public SpanPostProcessorProvider(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }

        this.version = TraceDataFormatVersion.getTraceDataFormatVersion(profilerConfig);
    }

    @Override
    public SpanPostProcessor get() {
        if (version == TraceDataFormatVersion.V2) {
            return new SpanPostProcessorV2();
        }
        if (version == TraceDataFormatVersion.V1) {
            return new SpanPostProcessorV1();
        }
        throw new UnsupportedOperationException("unknown version :" + version);
    }

}
