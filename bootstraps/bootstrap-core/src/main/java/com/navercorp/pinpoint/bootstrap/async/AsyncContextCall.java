package com.navercorp.pinpoint.bootstrap.async;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;

public interface AsyncContextCall {
    AsyncContext getAsyncContext(Object object);

    AsyncContext getAsyncContext(Object[] array, int index);
}
