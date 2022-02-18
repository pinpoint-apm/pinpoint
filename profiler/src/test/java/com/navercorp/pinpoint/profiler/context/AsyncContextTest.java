package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.provider.BaseTraceFactoryProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public abstract class AsyncContextTest {
    @Mock
    protected TraceRoot traceRoot;
    @Mock
    protected AsyncId asyncId;

    abstract AsyncContext newAsyncContext(final boolean canSampled);

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

    @Before
    public void setUp() {
        LocalAsyncId localAsyncId = new DefaultLocalAsyncId(0, 0);
        when(asyncId.nextLocalAsyncId()).thenReturn(localAsyncId);
    }

    @Test
    public void testAsyncTraceObject() {
        AsyncContext enabledAsyncContext = newAsyncContext(true);
        AsyncContext disabledAsyncContext = newAsyncContext(false);

        // at first, trace object must be null
        assertNull(enabledAsyncContext.currentAsyncTraceObject());
        assertNull(disabledAsyncContext.currentAsyncTraceObject());

        // invoke continueTraceObject
        Trace enabledTrace = enabledAsyncContext.continueAsyncTraceObject();
        Trace disabledTrace = disabledAsyncContext.continueAsyncTraceObject();
        assertTrue(enabledTrace instanceof AsyncChildTrace);
        assertNull(disabledTrace);

        // check current trace object
        assertEquals(enabledTrace, enabledAsyncContext.currentAsyncTraceObject());
        assertNull(disabledAsyncContext.currentAsyncTraceObject());

        // re-invocation of continueTraceObject must not change trace object
        Trace anotherEnabledTrace = enabledAsyncContext.continueAsyncTraceObject();
        assertEquals(enabledTrace, anotherEnabledTrace);
    }

    @Test
    public void testClose() {
        AsyncContext asyncContext = newAsyncContext(true);

        // invoke continueTraceObject
        Trace trace = asyncContext.continueAsyncTraceObject();
        assertNotNull(trace);

        // close
        asyncContext.close();
        assertNull(asyncContext.currentAsyncTraceObject());
    }
}
