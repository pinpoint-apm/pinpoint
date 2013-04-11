package com.profiler.modifier.db.interceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import com.profiler.logging.Logger;

import com.profiler.common.AnnotationKey;
import com.profiler.common.util.ParsingResult;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.interceptor.TraceContextSupport;
import com.profiler.interceptor.util.JDBCScope;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.LoggingUtils;
import com.profiler.modifier.db.DatabaseInfo;
import com.profiler.util.MetaObject;

public class PreparedStatementExecuteQueryInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(PreparedStatementExecuteQueryInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetaObject<Object> getSql = new MetaObject<Object>("__getSql");
    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");
    private final MetaObject<Map> getBindValue = new MetaObject<Map>("__getBindValue");
    private final MetaObject setBindValue = new MetaObject("__setBindValue", Map.class);

    private MethodDescriptor descriptor;
    private int apiId;
    private TraceContext traceContext;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
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
        try {
            DatabaseInfo databaseInfo = (DatabaseInfo) getUrl.invoke(target);

            trace.recordServiceType(databaseInfo.getExecuteQueryType());

            trace.recordEndPoint(databaseInfo.getMultipleHost());
            trace.recordDestinationId(databaseInfo.getDatabaseId());
            trace.recordDestinationAddress(databaseInfo.getHost());

            ParsingResult parsingResult = (ParsingResult) getSql.invoke(target);

            trace.recordSqlParsingResult(parsingResult);

            Map bindValue = getBindValue.invoke(target);
            String bindString = toBindVariable(bindValue);
            trace.recordAttribute(AnnotationKey.SQL_BINDVALUE, bindString);

            trace.recordApi(descriptor);
//            trace.recordApi(apiId);
            // clean 타이밍을 변경해야 될듯 하다.
            clean(target);


        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }

    }

    private void clean(Object target) {
        setBindValue.invoke(target, Collections.synchronizedMap(new HashMap()));
    }

    private String toBindVariable(Map bindValue) {
        String[] temp = new String[bindValue.size()];
        for (Object obj : bindValue.entrySet()) {
            Map.Entry<Integer, String> entry = (Map.Entry<Integer, String>) obj;
            Integer key = entry.getKey() - 1;
            if (temp.length < key) {
                continue;
            }
            temp[key] = entry.getValue();
        }
        return Arrays.toString(temp);
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
        }
        if (JDBCScope.isInternal()) {
            return;
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            // TODO 일단 테스트로 실패일경우 종료 아닐경우 resultset fetch까지 계산. fetch count는 옵션으로 빼는게 좋을듯.
            trace.recordException(result);
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        } finally {
            trace.markAfterTime();
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
