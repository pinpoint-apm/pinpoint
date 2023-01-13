package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DisableAsyncContext implements AsyncContext {
    private static final Logger logger = LogManager.getLogger(DisableAsyncContext.class);
    private final LocalTraceRoot traceRoot;
    private final AsyncTraceContext asyncTraceContext;
    private final Binder<Trace> binder;

    public DisableAsyncContext(AsyncTraceContext asyncTraceContext, Binder<Trace> binder, LocalTraceRoot traceRoot) {
        this.asyncTraceContext = Objects.requireNonNull(asyncTraceContext, "asyncTraceContext");
        this.binder = Objects.requireNonNull(binder, "binder");
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
    }

    @Override
    public Trace continueAsyncTraceObject() {
        final Reference<Trace> reference = binder.get();
        final Trace nestedTrace = reference.get();
        if (nestedTrace != null) {
            return nestedTrace;
        }

        return newAsyncContextTrace(reference);
    }

    private Trace newAsyncContextTrace(Reference<Trace> reference) {
        final Trace asyncTrace = asyncTraceContext.continueDisableAsyncContextTraceObject(traceRoot);
        bind(reference, asyncTrace);

        if (logger.isDebugEnabled()) {
            logger.debug("asyncTraceContext.continueDisableAsyncContextTraceObject() AsyncTrace:{}", asyncTrace);
        }

        if (AsyncScopeUtils.nested(asyncTrace, ASYNC_TRACE_SCOPE)) {
            return null;
        }

        return asyncTrace;
    }

    private void bind(Reference<Trace> reference, Trace asyncTrace) {
        Assert.state(reference.get() == null, "traceReference is  null");

        reference.set(asyncTrace);
    }

    @Override
    public Trace currentAsyncTraceObject() {
        final Reference<Trace> reference = binder.get();
        return reference.get();
    }

    @Override
    public void close() {
        binder.remove();
    }
}
