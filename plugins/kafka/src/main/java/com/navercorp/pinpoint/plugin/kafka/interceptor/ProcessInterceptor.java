/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.kafka.field.getter.RecordCollectorGetter;
import org.apache.kafka.streams.processor.internals.RecordCollector;

public class ProcessInterceptor implements AroundInterceptor {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public ProcessInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);

        if (asyncContext == null) {
            return;
        }

        if (!(target instanceof RecordCollectorGetter)) {
            return;

        }
        RecordCollectorGetter recordCollectorGetter = (RecordCollectorGetter) target;
        RecordCollector recordCollector = recordCollectorGetter._$PINPOINT$_getRecordCollector();

        AsyncContextAccessorUtils.setAsyncContext(asyncContext, recordCollector);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }

}
