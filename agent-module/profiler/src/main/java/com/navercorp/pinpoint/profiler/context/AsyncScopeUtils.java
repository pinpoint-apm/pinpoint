package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AsyncScopeUtils {
    private static final Logger logger = LogManager.getLogger(AsyncScopeUtils.class);

    private AsyncScopeUtils() {
    }

    public static boolean nested(Trace trace, String scopeName) {
        // add async scope.
        final TraceScope oldScope = trace.addScope(scopeName);
        if (oldScope != null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Duplicated {} scope={}", trace.getClass().getSimpleName(), oldScope.getName());
            }
            // delete corrupted trace.
//            deleteAsyncTrace(trace);
            return true;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("start {} scope", trace.getClass().getSimpleName());
            }
        }
        return false;
    }
}
