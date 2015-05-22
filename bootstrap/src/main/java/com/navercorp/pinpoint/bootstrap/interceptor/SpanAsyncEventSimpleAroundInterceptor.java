package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;

public abstract class SpanAsyncEventSimpleAroundInterceptor implements SimpleAroundInterceptor {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final MethodDescriptor methodDescriptor;
    protected final TraceContext traceContext;
    private MetadataAccessor asyncTraceIdAccessor;
    private boolean async = false;
    final MethodDescriptor asyncMethodDescriptor = new AsyncMethodDescriptor();

    public SpanAsyncEventSimpleAroundInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, MetadataAccessor asyncTraceIdAccessor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;

        traceContext.cacheApi(asyncMethodDescriptor);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!asyncTraceIdAccessor.isApplicable(target) || asyncTraceIdAccessor.get(target) == null) {
            logger.debug("Not found asynchronous invocation metadata");
            return;
        }

        final AsyncTraceId asyncTraceId = asyncTraceIdAccessor.get(target);
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            trace = traceContext.continueAsyncTraceObject(asyncTraceId, asyncTraceId.getAsyncId(), asyncTraceId.getSpanStartTime());
            if (trace == null) {
                logger.warn("Failed to continue async trace. 'result is null'");
                return;
            }
            traceFirstBlockBegin(trace);
            async = true;
        }

        try {
            trace.traceBlockBegin();
            doInBeforeTrace(trace, asyncTraceId, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("before. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private void traceFirstBlockBegin(final Trace trace) {
        // first block
        trace.traceBlockBegin();
        trace.markBeforeTime();
        trace.recordServiceType(ServiceType.ASYNC);
        trace.recordApi(asyncMethodDescriptor);
    }
    
    private void traceFirstBlockEnd(final Trace trace) {
        // first block
        trace.markAfterTime();
        trace.traceBlockEnd();
    }

    protected abstract void doInBeforeTrace(Trace trace, AsyncTraceId asyncTraceId, Object target, Object[] args);

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            doInAfterTrace(trace, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            if (async) {
                traceFirstBlockEnd(trace);
                trace.close();
                traceContext.removeTraceObject();
            }
        }
    }

    protected abstract void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable);

    public class AsyncMethodDescriptor implements MethodDescriptor {

        private int apiId = 0;

        @Override
        public String getMethodName() {
            return "";
        }

        @Override
        public String getClassName() {
            return "";
        }

        @Override
        public String[] getParameterTypes() {
            return null;
        }

        @Override
        public String[] getParameterVariableName() {
            return null;
        }

        @Override
        public String getParameterDescriptor() {
            return "";
        }

        @Override
        public int getLineNumber() {
            return -1;
        }

        @Override
        public String getFullName() {
            return AsyncMethodDescriptor.class.getName();
        }

        @Override
        public void setApiId(int apiId) {
            this.apiId = apiId;
        }

        @Override
        public int getApiId() {
            return apiId;
        }

        @Override
        public String getApiDescriptor() {
            return "Asynchronous Invocation";
        }

        @Override
        public int getType() {
            return 200;
        }
    }
}