package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatefulAsyncContextTest extends AsyncContextTest {
    @Mock
    private AsyncState asyncState;

    @Override
    AsyncContext newAsyncContext(boolean canSampled) {
        Binder<Trace> binder = new ThreadLocalBinder<>();
        AsyncTraceContext asyncTraceContext = newAsyncTraceContext();
        if (canSampled) {
            return AsyncContexts.remote(asyncTraceContext, binder, 0).async(traceRoot, asyncState, asyncId);
        } else {
            return AsyncContexts.local(asyncTraceContext, binder).sync(traceRoot);
        }
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void testGetAsyncState() {
        DefaultAsyncContext asyncContext = (DefaultAsyncContext) newAsyncContext(true);

        assertEquals(asyncState, asyncContext.getAsyncState());
    }
}
