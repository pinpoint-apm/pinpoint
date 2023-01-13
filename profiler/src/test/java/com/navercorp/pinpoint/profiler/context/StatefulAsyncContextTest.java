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
            return new StatefulAsyncContext(asyncTraceContext, binder, traceRoot, asyncId, 0, asyncState);
        } else {
            return new DisableAsyncContext(asyncTraceContext, binder, traceRoot);
        }
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void testGetAsyncState() {
        StatefulAsyncContext asyncContext = (StatefulAsyncContext) newAsyncContext(true);

        assertEquals(asyncState, asyncContext.getAsyncState());
    }
}
