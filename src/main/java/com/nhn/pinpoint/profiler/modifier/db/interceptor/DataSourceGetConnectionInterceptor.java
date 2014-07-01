package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

import com.nhn.pinpoint.profiler.util.DepthScope;

/**
 * Datasource의 get을 추적해야 될것으로 예상됨.
 * @author emeroad
 */
public class DataSourceGetConnectionInterceptor extends SpanEventSimpleAroundInterceptor {

    private final DepthScope scope = JDBCScope.SCOPE;

    public DataSourceGetConnectionInterceptor() {
        super(PLoggerFactory.getLogger(DataSourceGetConnectionInterceptor.class));
    }

    @Override
    protected void prepareBeforeTrace(Object target, Object[] args) {
        // 예외 케이스 : getConnection()에서 Driver.connect()가 호출되는지 알고 싶으므로 push만 한다.
        scope.push();
    }

    @Override
    public void doInBeforeTrace(Trace trace, final Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
        // 예외 케이스 : getConnection()에서 Driver.connect()가 호출되는지 알고 싶으므로 pop만 한다.
        scope.pop();
    }

    @Override
    public void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordServiceType(ServiceType.DBCP);
        if (args == null) {
//                args == null인 경우 parameter가 없는 getConnection() 호출시
            trace.recordApi(getMethodDescriptor());
        } else if(args.length == 2) {
//                args[1]은 패스워드라서 뺀다.
            trace.recordApi(getMethodDescriptor(), args[0], 0);
        }
        trace.recordException(throwable);

        trace.markAfterTime();
    }


}
