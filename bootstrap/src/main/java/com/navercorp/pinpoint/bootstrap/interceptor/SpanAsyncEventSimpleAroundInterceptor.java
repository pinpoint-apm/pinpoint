package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
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
            logger.beforeInterceptor(target, methodDescriptor.getClassName(), methodDescriptor.getMethodName(), "", args);
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
            if(isDebug) {
                logger.debug("Continue async trace. {}", asyncTraceId);
            }

            traceFirstBlockBegin(trace);
        }

        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            doInBeforeTrace(recorder, asyncTraceId, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("before. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private void traceFirstBlockBegin(final Trace trace) {
        // first block
        final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        recorder.recordServiceType(ServiceType.ASYNC);
        recorder.recordApi(asyncMethodDescriptor);
    }
    

    protected abstract void doInBeforeTrace(SpanEventRecorder recorder, AsyncTraceId asyncTraceId, Object target, Object[] args);

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, methodDescriptor.getClassName(), methodDescriptor.getMethodName(), "", args, result, throwable);
        }

        if (!asyncTraceIdAccessor.isApplicable(target) || asyncTraceIdAccessor.get(target) == null) {
            logger.debug("Not found asynchronous invocation metadata");
            return;
        }
        
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            doInAfterTrace(recorder, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            if (trace.isAsync() && trace.isRootStack()) {
                if(isDebug) {
                    logger.debug("Close async trace. {}");
                }

                traceFirstBlockEnd(trace);
                trace.close();
                traceContext.removeTraceObject();
            }
        }
    }

    private void traceFirstBlockEnd(final Trace trace) {
    }

    protected abstract void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable);

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