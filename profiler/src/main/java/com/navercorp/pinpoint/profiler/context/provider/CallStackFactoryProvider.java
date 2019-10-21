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
import com.google.inject.name.Named;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.CallStackFactory;
import com.navercorp.pinpoint.profiler.context.CallStackFactoryV1;
import com.navercorp.pinpoint.profiler.context.CallStackFactoryV2;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CallStackFactoryProvider implements Provider<CallStackFactory<SpanEvent>> {

    private final TraceDataFormatVersion version;
    private final int callStackMaxDepth;


    @Inject
    public CallStackFactoryProvider(@Named("profiler.callstack.max.depth") int callStackMaxDepth,
                                    TraceDataFormatVersion version) {
        this.version = Assert.requireNonNull(version, "version");
        this.callStackMaxDepth = callStackMaxDepth;
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
