package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.util.InterceptorUtils;
import com.nhn.pinpoint.profiler.util.MetaObject;

/**
 * @author emeroad
 */
public class PreparedStatementCreateInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor descriptor;

    // connection 용.
    private final MetaObject<DatabaseInfo> getDatabaseInfo = new MetaObject<DatabaseInfo>(UnKnownDatabaseInfo.INSTANCE, "__getDatabaseInfo");
    private final MetaObject setUrl = new MetaObject("__setDatabaseInfo", Object.class);

    private final MetaObject setSql = new MetaObject("__setSql", Object.class);
    private TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        trace.markBeforeTime();

        DatabaseInfo databaseInfo = getDatabaseInfo.invoke(target);
        if (databaseInfo == null) {
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }
        trace.recordServiceType(databaseInfo.getType());
        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());


    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }

        boolean success = InterceptorUtils.isSuccess(result);
        ParsingResult parsingResult = null;
        if (success) {
            // preparedStatement의 생성이 성공하였을 경우만 PreparedStatement에 databaseInfo를 세팅해야 한다.
            DatabaseInfo databaseInfo = getDatabaseInfo.invoke(target);
            if (databaseInfo != null) {
                this.setUrl.invoke(result, databaseInfo);
            }
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
        try {
            trace.recordSqlParsingResult(parsingResult);
            trace.recordException(result);
            trace.recordApi(descriptor);

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
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
