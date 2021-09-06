/*
 * Copyright 2018 NAVER Corp.
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
import com.navercorp.pinpoint.profiler.context.CallStackFactory;
import com.navercorp.pinpoint.profiler.context.CallStackFactoryV1;
import com.navercorp.pinpoint.profiler.context.CallStackFactoryV2;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CallStackFactoryProvider implements Provider<CallStackFactory<SpanEvent>> {

    private final TraceDataFormatVersion version;
    private final int callStackMaxDepth;


    @Inject
    public CallStackFactoryProvider(InstrumentConfig instrumentConfig,
                                    TraceDataFormatVersion version) {
        this.version = Objects.requireNonNull(version, "version");
        this.callStackMaxDepth = instrumentConfig.getCallStackMaxDepth();
    }

    @Override
    public CallStackFactory<SpanEvent> get() {
        if (version == TraceDataFormatVersion.V2) {
            return new CallStackFactoryV2(callStackMaxDepth);
        }
        if (version == TraceDataFormatVersion.V1) {
            return new CallStackFactoryV1(callStackMaxDepth);
        }
        throw new UnsupportedOperationException("unknown version :" + version);
    }

}
