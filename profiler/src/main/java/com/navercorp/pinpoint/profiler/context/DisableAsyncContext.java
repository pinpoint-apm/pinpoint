package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DisableAsyncContext implements AsyncContext {
    private static final Logger logger = LogManager.getLogger(DisableAsyncContext.class);

    private final AsyncContexts.Local local;
    private final LocalTraceRoot traceRoot;
    @Nullable
    private final AsyncState asyncState;

    DisableAsyncContext(AsyncContexts.Local local,
                        LocalTraceRoot traceRoot,
                        @Nullable AsyncState asyncState) {
        this.local = Objects.requireNonNull(local, "local");
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.asyncState = asyncState;
    }

    @Override
    public Trace continueAsyncTraceObject() {
        final Reference<Trace> reference = local.binder().get();
        final Trace nestedTrace = reference.get();
        if (nestedTrace != null) {
            return nestedTrace;
        }

        return newAsyncContextTrace(reference);
    }

    private Trace newAsyncContextTrace(Reference<Trace> reference) {
        final Trace asyncTrace = local.asyncTraceContext().continueDisableAsyncContextTraceObject(traceRoot);
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
        final Reference<Trace> reference = local.binder().get();
        return reference.get();
    }

    @Override
    public void close() {
        local.binder().remove();
    }


    @Override
    public boolean finish() {
        final AsyncState copy = this.asyncState;
        if (copy != null) {
            copy.finish();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "DisableAsyncContext{" +
                "traceRoot=" + traceRoot +
                ", asyncState=" + asyncState +
                '}';
    }
}
