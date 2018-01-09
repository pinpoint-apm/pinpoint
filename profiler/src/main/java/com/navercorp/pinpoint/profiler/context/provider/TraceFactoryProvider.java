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
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.Binder;
import com.navercorp.pinpoint.profiler.context.DefaultTraceFactory;
import com.navercorp.pinpoint.profiler.context.BaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.TraceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceFactoryProvider implements Provider<TraceFactory> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Binder<Trace> binder;

    private final Provider<BaseTraceFactory> baseTraceFactoryProvider;


    @Inject
    public TraceFactoryProvider(Provider<BaseTraceFactory> baseTraceFactoryProvider, Binder<Trace> binder) {

        this.baseTraceFactoryProvider = Assert.requireNonNull(baseTraceFactoryProvider, "baseTraceFactoryProvider must not be null");
        this.binder = Assert.requireNonNull(binder, "binder must not be null");

    }

    @Override
    public TraceFactory get() {

        final BaseTraceFactory baseTraceFactory = baseTraceFactoryProvider.get();

        TraceFactory traceFactory = new DefaultTraceFactory(baseTraceFactory, binder);

        return traceFactory;
    }


}
