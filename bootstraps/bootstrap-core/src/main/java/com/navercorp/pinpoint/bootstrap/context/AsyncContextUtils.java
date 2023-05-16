package com.navercorp.pinpoint.bootstrap.context;

public final class AsyncContextUtils {
    private AsyncContextUtils() {
    }

    public static boolean asyncStateFinish(final AsyncContext asyncContext) {
        if (asyncContext != null) {
            return asyncContext.finish();
        }
        return false;
    }
}
