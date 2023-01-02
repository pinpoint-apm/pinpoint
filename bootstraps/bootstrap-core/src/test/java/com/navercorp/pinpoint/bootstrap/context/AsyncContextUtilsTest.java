package com.navercorp.pinpoint.bootstrap.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AsyncContextUtilsTest {

    @Test
    void finish_success() {
        AsyncState state = mock(AsyncState.class);
        TestAsyncContext context = mock(TestAsyncContext.class);
        when(context.getAsyncState()).thenReturn(state);

        Assertions.assertTrue(AsyncContextUtils.asyncStateFinish(context));
    }

    @Test
    void finish_fail() {
        AsyncContext context = mock(AsyncContext.class);

        Assertions.assertFalse(AsyncContextUtils.asyncStateFinish(context));
    }

    interface TestAsyncContext extends AsyncContext, AsyncStateSupport {
    }

}