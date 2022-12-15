package com.navercorp.pinpoint.bootstrap.context;

public final class AsyncContextUtils {
    private AsyncContextUtils() {
    }

    public static boolean asyncStateFinish(final AsyncContext asyncContext) {
        if (asyncContext instanceof AsyncStateSupport) {
            final AsyncStateSupport asyncStateSupport = (AsyncStateSupport) asyncContext;
            AsyncState asyncState = asyncStateSupport.getAsyncState();
            asyncState.finish();
            return true;
        }
        return false;
    }
}
