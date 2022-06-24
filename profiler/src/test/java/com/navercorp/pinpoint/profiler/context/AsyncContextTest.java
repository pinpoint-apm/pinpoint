package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.provider.BaseTraceFactoryProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    public void setUp() {
        LocalAsyncId localAsyncId = new DefaultLocalAsyncId(0, 0);
        when(asyncId.nextLocalAsyncId()).thenReturn(localAsyncId);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
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
    @MockitoSettings(strictness = Strictness.LENIENT)
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
