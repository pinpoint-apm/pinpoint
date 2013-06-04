package com.nhn.pinpoint.modifier.db.interceptor;

import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.interceptor.util.JDBCScope;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.modifier.db.DatabaseInfo;
import com.nhn.pinpoint.profiler.util.InterceptorUtils;
import com.nhn.pinpoint.profiler.util.MetaObject;

import com.nhn.pinpoint.profiler.logging.Logger;

public class PreparedStatementCreateInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(PreparedStatementCreateInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor descriptor;

    // connection 용.
    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");
    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);

    private final MetaObject setSql = new MetaObject("__setSql", Object.class);
    private TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        if (JDBCScope.isInternal()) {
            logger.debug("internal jdbc scope. skip trace");
            return;
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        trace.markBeforeTime();

        DatabaseInfo databaseInfo = (DatabaseInfo) getUrl.invoke(target);

        trace.recordServiceType(databaseInfo.getType());

        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());
        trace.recordDestinationAddress(databaseInfo.getHost());

    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }
        if (JDBCScope.isInternal()) {
            logger.debug("internal jdbc scope. skip trace");
            return;
        }
        boolean success = InterceptorUtils.isSuccess(result);
        ParsingResult parsingResult = null;
        if (success) {
            // preparedStatement의 생성이 성공하였을 경우만 PreparedStatement에 databaseInfo를 세팅해야 한다.
            DatabaseInfo databaseInfo = (DatabaseInfo) getUrl.invoke(target);
            this.setUrl.invoke(result, databaseInfo);
            // 1. traceContext를 체크하면 안됨. traceContext에서 즉 같은 thread에서 prearedStatement에서 안만들수도 있음.
            // 2. sampling 동작이 동작할 경우 preparedStatement를 create하는 thread가 trace 대상이 아닐수 있음. 먼제 sql을 저장해야 한다.
            String sql = (String) args[0];
            parsingResult = traceContext.parseSql(sql);
            if (parsingResult != null) {
                this.setSql.invoke(result, parsingResult);
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("sqlParsing fail. parsingResult is null sql:{}", sql);
                }
            }
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.recordSqlParsingResult(parsingResult);
        trace.recordException(result);
        trace.recordApi(descriptor);

        trace.markAfterTime();
        trace.traceBlockEnd();
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
