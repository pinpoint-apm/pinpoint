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

import com.google.inject.Provider;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The {@code Provider<AsyncContextFactory>} breaks a Guice construction cycle, but every
 * {@code Provider.get()} enters a new Guice {@code InternalContext}. The factory must therefore
 * resolve the singleton lazily and exactly once, never eagerly in the constructor.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultRecorderFactoryTest {

    @Mock
    private Provider<AsyncContextFactory> asyncContextFactoryProvider;
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
        when(asyncContextFactoryProvider.get()).thenReturn(asyncContextFactory);
        when(exceptionRecorderFactory.newRecorder(any(TraceRoot.class))).thenReturn(mock(ExceptionRecorder.class));
        when(errorRecorderFactory.newRecorder(any(LocalTraceRoot.class))).thenReturn(mock(ErrorRecorder.class));

        this.factory = new DefaultRecorderFactory(asyncContextFactoryProvider, stringMetaDataService, sqlMetaDataService,
                errorHandler, exceptionRecorderFactory, errorRecorderFactory, sqlCountService);
    }

    @Test
    void constructor_doesNotResolveProvider() {
        // resolving eagerly would re-introduce the construction cycle the Provider exists to break
        verify(asyncContextFactoryProvider, never()).get();
    }

    @Test
    void asyncContextFactory_isResolvedOnceAcrossAllRecorderKinds() {
        Assertions.assertNotNull(factory.newWrappedSpanEventRecorder(traceRoot));
        Assertions.assertNotNull(factory.newWrappedSpanEventRecorder(traceRoot, asyncState));
        Assertions.assertNotNull(factory.newChildTraceSpanEventRecorder(traceRoot));
        Assertions.assertNotNull(factory.newDisableSpanEventRecorder(localTraceRoot));
        Assertions.assertNotNull(factory.newDisableSpanEventRecorder(localTraceRoot, asyncState));
        Assertions.assertNotNull(factory.newDisableChildTraceSpanEventRecorder(localTraceRoot, asyncState));
        // a second round must not touch the provider again
        Assertions.assertNotNull(factory.newChildTraceSpanEventRecorder(traceRoot));

        verify(asyncContextFactoryProvider, times(1)).get();
    }

    @Test
    void concurrentFirstCalls_resolveTheSameSingleton() throws Exception {
        final int threads = 8;
        final AtomicInteger providerCalls = new AtomicInteger();
        final AsyncContextFactory singleton = mock(AsyncContextFactory.class);
        when(asyncContextFactoryProvider.get()).thenAnswer(invocation -> {
            providerCalls.incrementAndGet();
            return singleton;
        });

        final CountDownLatch start = new CountDownLatch(1);
        final ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit((Callable<Object>) () -> {
                    start.await();
                    return factory.newChildTraceSpanEventRecorder(traceRoot);
                }));
            }
            start.countDown();
            for (Future<Object> future : futures) {
                Assertions.assertNotNull(future.get());
            }
        } finally {
            executor.shutdownNow();
        }

        // the benign race may call the provider more than once, but every call yields the same singleton
        final int callsAfterRace = providerCalls.get();
        Assertions.assertTrue(callsAfterRace >= 1 && callsAfterRace <= threads);
        // once warmed up the cache is authoritative: no further provider calls
        factory.newChildTraceSpanEventRecorder(traceRoot);
        factory.newWrappedSpanEventRecorder(traceRoot);
        Assertions.assertEquals(callsAfterRace, providerCalls.get());
    }
}
