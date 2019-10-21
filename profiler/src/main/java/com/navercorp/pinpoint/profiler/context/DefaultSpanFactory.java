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

package com.navercorp.pinpoint.profiler.context;

import com.google.inject.Inject;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultSpanFactory implements SpanFactory {


    @Inject
    public DefaultSpanFactory() {
    }

    @Override
    public Span newSpan(TraceRoot traceRoot) {
        Assert.requireNonNull(traceRoot, "traceRoot");

        final Span span = new Span(traceRoot);

        span.markBeforeTime();
        return span;
    }

}
