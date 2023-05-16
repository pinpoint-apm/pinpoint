package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.provider.BaseTraceFactoryProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

        when(baseTraceFactory.continueAsyncContextTraceObject(any(TraceRoot.class), any(LocalAsyncId.class)))
                .thenAnswer(new Answer<Trace>() {
                    @Override
                    public Trace answer(InvocationOnMock invocationOnMock) {
                        Trace trace = mock(AsyncChildTrace.class);
                        when(trace.canSampled()).thenReturn(true);
                        return trace;
                    }
                });
        when(baseTraceFactory.continueDisableAsyncContextTraceObject(any(TraceRoot.class)))
                .thenAnswer(new Answer<Trace>() {
                    @Override
                    public Trace answer(InvocationOnMock invocationOnMock) {
                        return mock(DisableAsyncChildTrace.class);
                    }
                });
        when(baseTraceFactoryProvider.get()).thenReturn(baseTraceFactory);

        return new DefaultAsyncTraceContext(baseTraceFactoryProvider);
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

}
