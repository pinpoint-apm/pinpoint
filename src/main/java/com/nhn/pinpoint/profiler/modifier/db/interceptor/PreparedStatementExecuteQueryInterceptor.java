package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.PLogger;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.util.MetaObject;

public class PreparedStatementExecuteQueryInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetaObject<Object> getSql = new MetaObject<Object>("__getSql");
    private final MetaObject<DatabaseInfo> getUrl = new MetaObject<DatabaseInfo>(UnKnownDatabaseInfo.INSTANCE, "__getDatabaseInfo");
    private final MetaObject<Map<Integer, String>> getBindValue = new MetaObject<Map<Integer, String>>("__getBindValue");
    private final MetaObject setBindValue = new MetaObject("__setBindValue", Map.class);

    private MethodDescriptor descriptor;
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
        try {
            DatabaseInfo databaseInfo = getUrl.invoke(target);
            trace.recordServiceType(databaseInfo.getExecuteQueryType());

            trace.recordEndPoint(databaseInfo.getMultipleHost());
            trace.recordDestinationId(databaseInfo.getDatabaseId());


            ParsingResult parsingResult = (ParsingResult) getSql.invoke(target);

            trace.recordSqlParsingResult(parsingResult);

            Map<Integer, String> bindValue = getBindValue.invoke(target);
            if (bindValue != null) {
                String bindString = toBindVariable(bindValue);
                if (bindString != null && bindString.length() != 0) {
                    trace.recordAttribute(AnnotationKey.SQL_BINDVALUE, bindString);
                }
            } else {
                logger.warn("bindValue not found.");
            }

            trace.recordApi(descriptor);
//            trace.recordApi(apiId);
            // clean 타이밍을 변경해야 될듯 하다.
            // clearParameters api가 따로 있으나, 구지 캡쳐 하지 않아도 될듯함.시간남으면 하면 좋기는 함.
            // ibatis 등에서 확인해봐도 cleanParameters 의 경우 대부분의 경우 일부러 호출하지 않음.
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

    private String toBindVariable(Map<Integer, String> bindValue) {
        String[] temp = new String[bindValue.size()];
        for (Map.Entry<Integer, String> entry : bindValue.entrySet()) {
            Integer key = entry.getKey() - 1;
            if (temp.length < key) {
                continue;
            }
            temp[key] = entry.getValue();
        }

        return bindValueToString(temp);

    }

    private String bindValueToString(String[] temp) {
        if (temp == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int end = temp.length - 1;
        for (int i = 0; i < temp.length; i++) {
            sb.append(temp[i]);
            if (i < end) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            // TODO 일단 테스트로 실패일경우 종료 아닐경우 resultset fetch까지 계산. fetch count는 옵션으로 빼는게 좋을듯.
            trace.recordException(result);
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
