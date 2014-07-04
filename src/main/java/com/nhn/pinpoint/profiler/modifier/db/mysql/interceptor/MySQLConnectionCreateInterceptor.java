package com.nhn.pinpoint.profiler.modifier.db.mysql.interceptor;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.util.InterceptorUtils;
import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public class MySQLConnectionCreateInterceptor implements SimpleAroundInterceptor, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        if (args == null || args.length != 5) {
            return;
        }

        final String hostToConnectTo = getString(args[0]);
        final Integer portToConnectTo = getInteger(args[1]);
        final String databaseId = getString(args[3]);
        // loadbalance 일경우 변형된 connectUrl이 온다.
//        final String url = getString(args[4]);
        DatabaseInfo databaseInfo = null;
        if (hostToConnectTo != null && portToConnectTo != null && databaseId != null) {
            // 여기의 url은 직접 사용하면 위험하다.
            databaseInfo = traceContext.createDatabaseInfo(ServiceType.MYSQL, ServiceType.MYSQL_EXECUTE_QUERY, hostToConnectTo, portToConnectTo, databaseId);
            if (InterceptorUtils.isSuccess(throwable)) {
                // connection이 정상 성공일때만 set해야 한다.
                if (target instanceof DatabaseInfoTraceValue) {
                    ((DatabaseInfoTraceValue)target).__setTraceDatabaseInfo(databaseInfo);
                }
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
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

}
