package com.navercorp.pinpoint.bootstrap.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AsyncContextUtilsTest {

    @Test
    void finish_success() {
        AsyncContext context = mock(AsyncContext.class);
        when(context.finish()).thenReturn(true);

        Assertions.assertTrue(AsyncContextUtils.asyncStateFinish(context));
        
    }

    @Test
    void finish_fail() {
        AsyncContext context = mock(AsyncContext.class);

        Assertions.assertFalse(AsyncContextUtils.asyncStateFinish(context));
        Assertions.assertFalse(AsyncContextUtils.asyncStateFinish(null));
    }


}