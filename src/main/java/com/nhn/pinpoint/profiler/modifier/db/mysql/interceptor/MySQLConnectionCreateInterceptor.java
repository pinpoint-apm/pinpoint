package com.nhn.pinpoint.profiler.modifier.db.mysql.interceptor;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.util.InterceptorUtils;
import com.nhn.pinpoint.bootstrap.util.MetaObject;
import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public class MySQLConnectionCreateInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor descriptor;
    private TraceContext traceContext;

    private final MetaObject setUrl = new MetaObject("__setDatabaseInfo", Object.class);

    // setUrl에서 String type은 databaseInfo로 변경되었다.
//    private final MetaObject setUrl = new MetaObject("__setDatabaseInfo", Object.class);

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }
        if (args == null || args.length != 5) {
            return;
        }

        final String url = getString(args[0]);
        final Integer port = getInteger(args[1]);
        final String databaseId = getString(args[3]);
        DatabaseInfo databaseInfo = null;
        if (url != null && port != null && databaseId != null) {
            databaseInfo = traceContext.createDatabaseInfo(ServiceType.MYSQL, ServiceType.MYSQL_EXECUTE_QUERY, url, port, databaseId);
            if (InterceptorUtils.isSuccess(result)) {
                // connection이 정상 성공일때만 set해야 한다.
                setUrl.invoke(target, databaseInfo);
            }
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        // 상위에서 레코딩 중일 경우반드시한다.
        if (databaseInfo != null) {
            trace.recordServiceType(databaseInfo.getExecuteQueryType());
            trace.recordEndPoint(databaseInfo.getMultipleHost());
            trace.recordDestinationId(databaseInfo.getDatabaseId());
        }

    }

    private String getString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    private Integer getInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return null;
    }

    @Override
    public void before(Object target, Object[] args) {

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
