package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.provider.BaseTraceFactoryProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAsyncTraceContextTest {
    private static final Reference<Trace> EMPTY = DefaultReference.emptyReference();

    @Mock
    private TraceRoot traceRoot;
    @Mock
    private LocalAsyncId localAsyncId;

    protected AsyncTraceContext newAsyncTraceContext() {
        BaseTraceFactory baseTraceFactory = mock(DefaultBaseTraceFactory.class);
        BaseTraceFactoryProvider baseTraceFactoryProvider = mock(BaseTraceFactoryProvider.class);

        when(baseTraceFactory.continueAsyncContextTraceObject(any(TraceRoot.class), any(LocalAsyncId.class), eq(true)))
            .thenAnswer(new Answer<Trace>() {
                @Override
                public Trace answer(InvocationOnMock invocationOnMock) {
                    Trace trace = mock(AsyncChildTrace.class);
                    when(trace.canSampled()).thenReturn(true);
                    return trace;
                }
            });
        when(baseTraceFactory.continueAsyncContextTraceObject(any(TraceRoot.class), any(LocalAsyncId.class), eq(false)))
            .thenAnswer(new Answer<Trace>() {
                @Override
                public Trace answer(InvocationOnMock invocationOnMock) {
                    return mock(DisableAsyncChildTrace.class);
                }
            });
        when(baseTraceFactoryProvider.get()).thenReturn(baseTraceFactory);

        return new DefaultAsyncTraceContext(baseTraceFactoryProvider, new ThreadLocalBinder<Trace>());
    }

    @Test
    public void testNewAsyncTraceObject() {
        AsyncTraceContext asyncTraceContext = newAsyncTraceContext();

        // new trace object
        Trace newTraceEnabled = asyncTraceContext.newAsyncContextTraceObject(traceRoot, localAsyncId, true);
        assertTrue(newTraceEnabled instanceof AsyncChildTrace);
        assertNull(asyncTraceContext.currentRawTraceObject().get());

        Trace newTraceDisabled = asyncTraceContext.newAsyncContextTraceObject(traceRoot, localAsyncId, false);
        assertTrue(newTraceDisabled instanceof DisableAsyncChildTrace);
        assertNull(asyncTraceContext.currentRawTraceObject().get());
    }

    @Test
    public void testAsyncTraceContext() {
        AsyncTraceContext asyncTraceContext = newAsyncTraceContext();

        // sampling enabled
        Reference<Trace> continueTraceEnabled = asyncTraceContext.continueAsyncContextTraceObject(traceRoot, localAsyncId, true);
        assertTrue(continueTraceEnabled.get() instanceof AsyncChildTrace);
        assertEquals(continueTraceEnabled, asyncTraceContext.currentRawTraceObject());
        assertEquals(continueTraceEnabled, asyncTraceContext.currentTraceObject());

        // remove trace object
        asyncTraceContext.removeTraceObject();
        assertNull(asyncTraceContext.currentRawTraceObject().get());

        // sampling disabled
        Reference<Trace> continueTraceDisabled = asyncTraceContext.continueAsyncContextTraceObject(traceRoot, localAsyncId, false);
        assertTrue(continueTraceDisabled.get() instanceof DisableAsyncChildTrace);
        assertEquals(continueTraceDisabled, asyncTraceContext.currentRawTraceObject());
        assertEquals(EMPTY, asyncTraceContext.currentTraceObject());
    }

    @Test(expected = PinpointException.class)
    public void testOverrideTrace() {
        AsyncTraceContext asyncTraceContext = newAsyncTraceContext();

        asyncTraceContext.continueAsyncContextTraceObject(traceRoot, localAsyncId, true);
        asyncTraceContext.continueAsyncContextTraceObject(traceRoot, localAsyncId, false);
    }
}
