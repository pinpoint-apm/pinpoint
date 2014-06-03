package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
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

        if (InterceptorUtils.isThrowable(result)) {
            return;
        }

        String url = getString(args[0]);
        Integer port = getInteger(args[1]);
        String databaseId = getString(args[3]);
        if (url != null && port != null && databaseId != null) {
            DatabaseInfo databaseInfo = traceContext.createDatabaseInfo(ServiceType.MYSQL, ServiceType.MYSQL_EXECUTE_QUERY, url, port, databaseId);
            setUrl.invoke(target, databaseInfo);
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
