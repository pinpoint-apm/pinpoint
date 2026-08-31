package com.navercorp.pinpoint.profiler.context;

import com.google.common.base.Suppliers;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.provider.BaseTraceFactoryProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultAsyncTraceContextTest {
    private static final Reference<Trace> EMPTY = DefaultReference.emptyReference();

    @Mock
    private TraceRoot traceRoot;
    @Mock
    private LocalAsyncId localAsyncId;

    protected AsyncTraceContext newAsyncTraceContext() {
        BaseTraceFactory baseTraceFactory = mock(DefaultBaseTraceFactory.class);
        BaseTraceFactoryProvider baseTraceFactoryProvider = mock(BaseTraceFactoryProvider.class);

        when(baseTraceFactory.continueAsyncContextTraceObject(any(TraceRoot.class), any(LocalAsyncId.class), any(Boolean.class)))
                .thenAnswer(new Answer<Trace>() {
                    @Override
                    public Trace answer(InvocationOnMock invocationOnMock) {
                        Trace trace = mock(ChildTrace.class);
                        when(trace.canSampled()).thenReturn(true);
                        return trace;
                    }
                });
        when(baseTraceFactory.continueDisableAsyncContextTraceObject(any(TraceRoot.class)))
                .thenAnswer(new Answer<Trace>() {
                    @Override
                    public Trace answer(InvocationOnMock invocationOnMock) {
                        return mock(DisableChildTrace.class);
                    }
                });
        when(baseTraceFactoryProvider.get()).thenReturn(baseTraceFactory);

        return new DefaultAsyncTraceContext(Suppliers.memoize(baseTraceFactoryProvider::get));
    }

//    @MockitoSettings(strictness = Strictness.LENIENT)
//    @Test
//    public void testNewAsyncTraceObject() {
//        AsyncTraceContext asyncTraceContext = newAsyncTraceContext();
//
//        // new trace object
//        Trace newTraceEnabled = asyncTraceContext.newAsyncContextTraceObject(traceRoot, localAsyncId, true);
//        assertTrue(newTraceEnabled instanceof AsyncChildTrace);
//        assertNull(asyncTraceContext.currentRawTraceObject().get());
//
//        Trace newTraceDisabled = asyncTraceContext.newAsyncContextTraceObject(traceRoot, localAsyncId, false);
//        assertTrue(newTraceDisabled instanceof DisableAsyncChildTrace);
//        assertNull(asyncTraceContext.currentRawTraceObject().get());
//    }

    @Test
    public void baseTraceFactoryProvider_isResolvedLazilyAndOnce() {
        BaseTraceFactory baseTraceFactory = mock(DefaultBaseTraceFactory.class);
        Provider<BaseTraceFactory> baseTraceFactoryProvider = mock(BaseTraceFactoryProvider.class);
        when(baseTraceFactoryProvider.get()).thenReturn(baseTraceFactory);
        when(baseTraceFactory.continueAsyncContextTraceObject(any(TraceRoot.class), any(LocalAsyncId.class), any(Boolean.class)))
                .thenReturn(mock(ChildTrace.class));
        when(baseTraceFactory.continueDisableAsyncContextTraceObject(any(LocalTraceRoot.class)))
                .thenReturn(mock(DisableChildTrace.class));

        AsyncTraceContext asyncTraceContext = new DefaultAsyncTraceContext(Suppliers.memoize(baseTraceFactoryProvider::get));
        // the Provider breaks a Guice construction cycle: it must not be resolved in the constructor
        verify(baseTraceFactoryProvider, never()).get();

        Assertions.assertNotNull(asyncTraceContext.continueAsyncContextTraceObject(traceRoot, localAsyncId, true));
        Assertions.assertNotNull(asyncTraceContext.continueAsyncContextTraceObject(traceRoot, localAsyncId, false));
        Assertions.assertNotNull(asyncTraceContext.continueDisableAsyncContextTraceObject(traceRoot));

        // every Provider.get() enters a new Guice InternalContext; the singleton must be cached after the first call
        verify(baseTraceFactoryProvider, times(1)).get();
        verify(baseTraceFactory, times(2)).continueAsyncContextTraceObject(any(TraceRoot.class), any(LocalAsyncId.class), any(Boolean.class));
        verify(baseTraceFactory, times(1)).continueDisableAsyncContextTraceObject(any(LocalTraceRoot.class));
    }

}
