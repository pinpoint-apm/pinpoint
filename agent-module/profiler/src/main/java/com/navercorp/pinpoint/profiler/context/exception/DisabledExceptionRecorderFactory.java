/*
 * Copyright 2024 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.context.exception;

import com.navercorp.pinpoint.profiler.context.exception.disabled.DisabledExceptionRecorder;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

public class DisabledExceptionRecorderFactory implements ExceptionRecorderFactory {

    public static final ExceptionRecorderFactory INSTANCE = new DisabledExceptionRecorderFactory();

    public DisabledExceptionRecorderFactory() {
    }

    @Override
    public ExceptionRecorder newService(TraceRoot traceRoot) {
        return DisabledExceptionRecorder.INSTANCE;
    }
}
