package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.ParsingResultTraceValue;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.util.InterceptorUtils;

/**
 * @author emeroad
 */
public class PreparedStatementCreateInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor descriptor;

    // connection 용.
    private TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        trace.markBeforeTime();

        final DatabaseInfo databaseInfo = getDatabaseInfo(target);
        trace.recordServiceType(databaseInfo.getType());
        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());


    }

    private DatabaseInfo getDatabaseInfo(Object target) {
        if (target instanceof DatabaseInfoTraceValue) {
            final DatabaseInfo databaseInfo = ((DatabaseInfoTraceValue)target).__getTraceDatabaseInfo();
            if (databaseInfo == null) {
                return UnKnownDatabaseInfo.INSTANCE;
            }
            return databaseInfo;
        }
        return UnKnownDatabaseInfo.INSTANCE;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }

        final boolean success = InterceptorUtils.isSuccess(throwable);
        ParsingResult parsingResult = null;
        if (success) {
            if (target instanceof DatabaseInfoTraceValue) {
                // preparedStatement의 생성이 성공하였을 경우만 PreparedStatement에 databaseInfo를 세팅해야 한다.
                DatabaseInfo databaseInfo = ((DatabaseInfoTraceValue) target).__getTraceDatabaseInfo();
                if (databaseInfo != null) {
                    if (result instanceof DatabaseInfoTraceValue) {
                        ((DatabaseInfoTraceValue) result).__setTraceDatabaseInfo(databaseInfo);
                    }
                }
            }
            if (result instanceof ParsingResultTraceValue) {
                // 1. traceContext를 체크하면 안됨. traceContext에서 즉 같은 thread에서 prearedStatement에서 안만들수도 있음.
                // 2. sampling 동작이 동작할 경우 preparedStatement를 create하는 thread가 trace 대상이 아닐수 있음. 먼제 sql을 저장해야 한다.
                String sql = (String) args[0];
                parsingResult = traceContext.parseSql(sql);
                if (parsingResult != null) {
                    ((ParsingResultTraceValue)result).__setTraceParsingResult(parsingResult);
                } else {
                    if (logger.isErrorEnabled()) {
                        logger.error("sqlParsing fail. parsingResult is null sql:{}", sql);
                    }
                }
            }
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            trace.recordSqlParsingResult(parsingResult);
            trace.recordException(throwable);
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
