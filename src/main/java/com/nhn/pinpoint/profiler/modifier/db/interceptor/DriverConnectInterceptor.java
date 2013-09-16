package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.interceptor.util.JDBCScope;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.util.InterceptorUtils;
import com.nhn.pinpoint.profiler.util.MetaObject;

import com.nhn.pinpoint.profiler.logging.PLogger;

/**
 *
 */
public class DriverConnectInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);

    private MethodDescriptor descriptor;
    private TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
            logger.debug("JDBCScope push:{}", Thread.currentThread().getName());
        }
        JDBCScope.push();

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        trace.markBeforeTime();

    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
            logger.debug("JDBCScope pop:{}", Thread.currentThread().getName());
        }
        // 여기서는 trace context인지 아닌지 확인하면 안된다. trace 대상 thread가 아닌곳에서 connection이 생성될수 있음.
        JDBCScope.pop();

        boolean success = InterceptorUtils.isSuccess(result);
        // 여기서는 trace context인지 아닌지 확인하면 안된다. trace 대상 thread가 아닌곳에서 connection이 생성될수 있음.
        DatabaseInfo databaseInfo = createDatabaseInfo((String) args[0]);
        if (success) {
            // 생성이 성공해야 result가 connection임.
            this.setUrl.invoke(result, databaseInfo);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            trace.recordServiceType(databaseInfo.getType());

            trace.recordEndPoint(databaseInfo.getMultipleHost());
            trace.recordDestinationId(databaseInfo.getDatabaseId());
            trace.recordDestinationAddress(databaseInfo.getHost());


            trace.recordApi(descriptor, new Object[]{args[0]});
            trace.recordException(result);

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }

    private DatabaseInfo createDatabaseInfo(String url) {
        DatabaseInfo databaseInfo = traceContext.parseJdbcUrl(url);
        if (logger.isDebugEnabled()) {
            logger.debug("parse DatabaseInfo:" + databaseInfo);
        }
        return databaseInfo;
    }


    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        traceContext.cacheApi(descriptor);
    }


    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}
