package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;


public class StatefulAsyncContextTest extends AsyncContextTest {
    @Mock
    private AsyncState asyncState;

    @Override
    AsyncContext newAsyncContext(boolean canSampled) {
        AsyncTraceContext asyncTraceContext = newAsyncTraceContext();
        return new StatefulAsyncContext(asyncTraceContext, traceRoot, asyncId, 0, asyncState, canSampled);
    }

    @Test
    public void testGetAsyncState() {
        StatefulAsyncContext asyncContext = (StatefulAsyncContext) newAsyncContext(true);

        assertEquals(asyncState, asyncContext.getAsyncState());
    }
}
