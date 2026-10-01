/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.recorder;

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.ErrorRecorder;
import com.navercorp.pinpoint.profiler.context.AsyncContextFactory;
import com.navercorp.pinpoint.profiler.context.SqlCountService;
import com.navercorp.pinpoint.profiler.context.error.ErrorRecorderFactory;
import com.navercorp.pinpoint.profiler.context.errorhandler.IgnoreErrorHandler;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecorder;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecorderFactory;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultRecorderFactoryTest {

    @Mock
    private AsyncContextFactory asyncContextFactory;
    @Mock
    private StringMetaDataService stringMetaDataService;
    @Mock
    private SqlMetaDataService sqlMetaDataService;
    @Mock
    private IgnoreErrorHandler errorHandler;
    @Mock
    private ExceptionRecorderFactory exceptionRecorderFactory;
    @Mock
    private ErrorRecorderFactory errorRecorderFactory;
    @Mock
    private SqlCountService sqlCountService;
    @Mock
    private TraceRoot traceRoot;
    @Mock
    private LocalTraceRoot localTraceRoot;
    @Mock
    private AsyncState asyncState;

    private DefaultRecorderFactory factory;

    @BeforeEach
    void setUp() {
        when(exceptionRecorderFactory.newRecorder(any(TraceRoot.class))).thenReturn(mock(ExceptionRecorder.class));
        when(errorRecorderFactory.newRecorder(any(LocalTraceRoot.class))).thenReturn(mock(ErrorRecorder.class));

        this.factory = new DefaultRecorderFactory(asyncContextFactory, stringMetaDataService, sqlMetaDataService,
                errorHandler, exceptionRecorderFactory, errorRecorderFactory, sqlCountService);
    }


    @Test
    void newRecorder_allRecorderKinds() {
        Assertions.assertNotNull(factory.newWrappedSpanEventRecorder(traceRoot));
        Assertions.assertNotNull(factory.newWrappedSpanEventRecorder(traceRoot, asyncState));
        Assertions.assertNotNull(factory.newChildTraceSpanEventRecorder(traceRoot));
        Assertions.assertNotNull(factory.newDisableSpanEventRecorder(localTraceRoot));
        Assertions.assertNotNull(factory.newDisableSpanEventRecorder(localTraceRoot, asyncState));
        Assertions.assertNotNull(factory.newDisableChildTraceSpanEventRecorder(localTraceRoot, asyncState));
        Assertions.assertNotNull(factory.newChildTraceSpanEventRecorder(traceRoot));
    }

}
