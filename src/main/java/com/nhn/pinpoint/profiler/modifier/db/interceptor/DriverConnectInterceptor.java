package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValueUtils;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.util.DepthScope;
import com.nhn.pinpoint.bootstrap.util.InterceptorUtils;


/**
 * @author emeroad
 */
public class DriverConnectInterceptor extends SpanEventSimpleAroundInterceptor {


    private final DepthScope scope = JDBCScope.SCOPE;
    private final boolean recordConnection;

    public DriverConnectInterceptor() {
        this(true);
    }

    public DriverConnectInterceptor(boolean recordConnection) {
        super(DriverConnectInterceptor.class);
        // mysql loadbalance 전용옵션 실제 destination은 하위의 구현체에서 레코딩한다.
        this.recordConnection = recordConnection;
    }

    @Override
    protected void logBeforeInterceptor(Object target, Object[] args) {
        // parameter에 암호가 포함되어 있음 로깅하면 안됨.
        logger.beforeInterceptor(target, null);
    }

    @Override
    protected void prepareBeforeTrace(Object target, Object[] args) {
        scope.push();
    }

    @Override
    protected void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
        trace.markBeforeTime();
    }


    @Override
    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, null, result, throwable);
    }

    @Override
    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
        // 여기서는 trace context인지 아닌지 확인하면 안된다. trace 대상 thread가 아닌곳에서 connection이 생성될수 있음.
        scope.pop();

        final boolean success = InterceptorUtils.isSuccess(throwable);
        // 여기서는 trace context인지 아닌지 확인하면 안된다. trace 대상 thread가 아닌곳에서 connection이 생성될수 있음.
        final String driverUrl = (String) args[0];
        DatabaseInfo databaseInfo = createDatabaseInfo(driverUrl);
        if (success) {
            if (recordConnection) {
                DatabaseInfoTraceValueUtils.__setTraceDatabaseInfo(result, databaseInfo);
            }
        }
    }

    @Override
    protected void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {

        if (recordConnection) {
            final DatabaseInfo databaseInfo = DatabaseInfoTraceValueUtils.__getTraceDatabaseInfo(result, UnKnownDatabaseInfo.INSTANCE);
            // database connect도 매우 무거운 액션이므로 카운트로 친다.
            trace.recordServiceType(databaseInfo.getExecuteQueryType());
            trace.recordEndPoint(databaseInfo.getMultipleHost());
            trace.recordDestinationId(databaseInfo.getDatabaseId());
        }
        final String driverUrl = (String) args[0];
        // 여기서 databaseInfo.getRealUrl을 하면 위험하다. loadbalance connection일때 원본 url이 아닌 url이 오게 되어 있음.
        trace.recordApiCachedString(getMethodDescriptor(), driverUrl, 0);

        trace.recordException(throwable);
        trace.markAfterTime();
    }

    private DatabaseInfo createDatabaseInfo(String url) {
        if (url == null) {
            return UnKnownDatabaseInfo.INSTANCE;
        }
        final DatabaseInfo databaseInfo = getTraceContext().parseJdbcUrl(url);
        if (isDebug) {
            logger.debug("parse DatabaseInfo:{}", databaseInfo);
        }
        return databaseInfo;
    }

}
