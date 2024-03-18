/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.recorder;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.profiler.context.AsyncContextFactory;
import com.navercorp.pinpoint.profiler.context.SqlCountService;
import com.navercorp.pinpoint.profiler.context.errorhandler.IgnoreErrorHandler;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecorder;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;

public class ChildTraceSpanEventRecorder extends WrappedSpanEventRecorder {

    public ChildTraceSpanEventRecorder(TraceRoot traceRoot,
                                       AsyncContextFactory asyncContextFactory,
                                       StringMetaDataService stringMetaDataService,
                                       SqlMetaDataService sqlMetaDataService,
                                       IgnoreErrorHandler ignoreErrorHandler,
                                       ExceptionRecorder exceptionRecorder,
                                       SqlCountService sqlCountService) {
        super(traceRoot, asyncContextFactory, stringMetaDataService, sqlMetaDataService, ignoreErrorHandler, exceptionRecorder, sqlCountService);
    }

    @Override
    public AsyncContext recordNextAsyncContext(boolean asyncStateSupport) {
        return super.recordNextAsyncContext();
    }
}
