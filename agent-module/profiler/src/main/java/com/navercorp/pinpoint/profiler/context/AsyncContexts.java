package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class AsyncContexts {
    private AsyncContexts() {
    }

    public static Remote remote(AsyncTraceContext asyncTraceContext,
                                Binder<Trace> binder,
                                int asyncMethodApiId) {
        return new Remote(asyncTraceContext, binder, asyncMethodApiId);
    }

    public static Local local(AsyncTraceContext asyncTraceContext,
                              Binder<Trace> binder) {
        return new Local(asyncTraceContext, binder);
    }

    public static class Remote {
        private final AsyncTraceContext asyncTraceContext;
        private final Binder<Trace> binder;
        private final int asyncMethodApiId;

        private Remote(AsyncTraceContext asyncTraceContext, Binder<Trace> binder, int asyncMethodApiId) {
            this.asyncTraceContext = Objects.requireNonNull(asyncTraceContext, "asyncTraceContext");
            this.binder = Objects.requireNonNull(binder, "binder");
            this.asyncMethodApiId = asyncMethodApiId;
        }

        public AsyncContext sync(TraceRoot traceRoot,
                                 AsyncId asyncId) {
            return new DefaultAsyncContext(this, traceRoot, asyncId, null);
        }

        public AsyncContext async(TraceRoot traceRoot,
                                  AsyncState asyncState,
                                  AsyncId asyncId) {
            return new DefaultAsyncContext(this, traceRoot, asyncId, asyncState);
        }

        public AsyncTraceContext asyncTraceContext() {
            return asyncTraceContext;
        }

        public Binder<Trace> binder() {
            return binder;
        }

        public int asyncMethodApiId() {
            return asyncMethodApiId;
        }
    }

    public static class Local {

        private final AsyncTraceContext asyncTraceContext;
        private final Binder<Trace> binder;

        private Local(AsyncTraceContext asyncTraceContext, Binder<Trace> binder) {
            this.asyncTraceContext = Objects.requireNonNull(asyncTraceContext, "asyncTraceContext");
            this.binder = Objects.requireNonNull(binder, "binder");
        }

        public AsyncContext sync(LocalTraceRoot traceRoot) {
            return new DisableAsyncContext(this, traceRoot, null);
        }

        public AsyncContext async(LocalTraceRoot traceRoot,
                                  AsyncState asyncState) {
            return new DisableAsyncContext(this, traceRoot, asyncState);
        }

        public AsyncTraceContext asyncTraceContext() {
            return asyncTraceContext;
        }

        public Binder<Trace> binder() {
            return binder;
        }
    }

}
