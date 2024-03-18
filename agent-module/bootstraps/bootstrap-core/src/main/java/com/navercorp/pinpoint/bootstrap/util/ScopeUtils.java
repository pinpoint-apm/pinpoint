package com.navercorp.pinpoint.bootstrap.util;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;

public final class ScopeUtils {

    public static final String ASYNC_TRACE_SCOPE = AsyncContext.ASYNC_TRACE_SCOPE;

    private ScopeUtils() {
    }

    public static void entryAsyncTraceScope(final Trace trace) {
        entryScope(trace, ASYNC_TRACE_SCOPE);
    }

    public static void entryScope(final Trace trace, final String scopeName) {
        final TraceScope scope = trace.getScope(scopeName);
        if (scope != null) {
            scope.tryEnter();
        }
    }

    public static boolean leaveAsyncTraceScope(final Trace trace) {
        return leaveScope(trace, ASYNC_TRACE_SCOPE);
    }

    public static boolean leaveScope(final Trace trace, final String scopeName) {
        final TraceScope scope = trace.getScope(scopeName);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean isAsyncTraceEndScope(final Trace trace) {
        return isAsyncTraceEndScope(trace, ASYNC_TRACE_SCOPE);
    }

    public static boolean isAsyncTraceEndScope(final Trace trace, final String scopeName) {
        if (!trace.isAsync()) {
            return false;
        }
        return isEndScope(trace, scopeName);
    }

    public static boolean isEndScope(final Trace trace, final String scopeName) {
        final TraceScope scope = trace.getScope(scopeName);
        return scope != null && !scope.isActive();
    }

    public static boolean hasScope(final Trace trace, final String scopeName) {
        final TraceScope scope = trace.getScope(scopeName);
        return scope != null;
    }

    public static boolean addScope(Trace trace, String scopeName) {
        // add async scope.
        final TraceScope oldScope = trace.addScope(scopeName);
        if (oldScope != null) {
            // delete corrupted trace.
//            deleteAsyncTrace(trace);
            return false;
        }
        return true;
    }
}
