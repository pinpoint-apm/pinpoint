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

package com.navercorp.pinpoint.profiler.context.active;

import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeCollector;

import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class EmptyActiveTraceRepository implements ActiveTraceRepository {

    private final ResponseTimeCollector responseTimeCollector;

    private final ActiveTraceHistogram emptyActiveTraceHistogram = new EmptyActiveTraceHistogram(BaseHistogramSchema.NORMAL_SCHEMA);

    public EmptyActiveTraceRepository(ResponseTimeCollector responseTimeCollector) {
        this.responseTimeCollector = Assert.requireNonNull(responseTimeCollector, "responseTimeCollector must not be null");
    }

    @Override
    public ActiveTraceHistogram getActiveTraceHistogram(long timeStamp) {
        return emptyActiveTraceHistogram;
    }

    @Override
    public List<ActiveTraceSnapshot> collect() {
        return Collections.emptyList();
    }

    @Override
    public ActiveTraceHandle register(TraceRoot traceRoot) {
        Assert.requireNonNull(traceRoot, "traceRoot must not be null");
        return new EmptyActiveTraceHandle(traceRoot.getTraceStartTime());
    }

    @Override
    public ActiveTraceHandle register(long localTransactionId, long startTime, long threadId) {
        return new EmptyActiveTraceHandle(startTime);
    }


    private void remove(long startTime, long purgeTime) {
        final long responseTime = purgeTime - startTime;
        responseTimeCollector.add(responseTime);
    }

    private class EmptyActiveTraceHandle implements ActiveTraceHandle {
        private final long startTime;

        public EmptyActiveTraceHandle(long startTime) {
            this.startTime = startTime;
        }

        @Override
        public void purge(long purgeTime) {
            remove(startTime, purgeTime);
        }
    };

}
