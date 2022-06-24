package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
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
        AsyncTraceContext asyncTraceContext = newAsyncTraceContext();
        return new StatefulAsyncContext(asyncTraceContext, traceRoot, asyncId, 0, asyncState, canSampled);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void testGetAsyncState() {
        StatefulAsyncContext asyncContext = (StatefulAsyncContext) newAsyncContext(true);

        assertEquals(asyncState, asyncContext.getAsyncState());
    }
}
