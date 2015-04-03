package com.navercorp.pinpoint.plugin.httpclient4.interceptor;

import org.apache.http.concurrent.BasicFuture;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.Name;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;

public class DefaultClientExchangeHandlerImplConstructorInterceptor implements SimpleAroundInterceptor, HttpClient4Constants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MetadataAccessor asyncTraceIdAccessor;

    public DefaultClientExchangeHandlerImplConstructorInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor) {
        this.traceContext = traceContext;
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            if (!validate(target, args)) {
                return;
            }

            final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
            trace.recordnextAsyncId(asyncTraceId.getAsyncId());
            asyncTraceIdAccessor.set(args[4], asyncTraceId);
            logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (args == null || args.length < 6 || args[4] == null) {
            logger.debug("Invalid arguments. Null or not found args({}).", args);
            return false;
        }

        if (!(args[4] instanceof BasicFuture)) {
            logger.debug("Invalid arguments. Expect BasicFuture but args[0]({}).", args[4]);
            return false;
        }

        if (!asyncTraceIdAccessor.isApplicable(args[4])) {
            logger.debug("Invalid target object. Need metadata accessor({}).", METADATA_ASYNC_TRACE_ID);
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
