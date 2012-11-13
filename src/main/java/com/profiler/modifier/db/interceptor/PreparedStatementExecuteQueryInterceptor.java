package com.profiler.modifier.db.interceptor;

import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreparedStatementExecuteQueryInterceptor implements StaticAroundInterceptor {

    private final Logger logger = Logger.getLogger(PreparedStatementExecuteQueryInterceptor.class.getName());

    private final MetaObject<String> getSql = new MetaObject<String>("__getSql");
    private final MetaObject<String> getUrl = new MetaObject<String>("__getUrl");
    private final MetaObject<Map> getBindValue = new MetaObject<Map>("__getBindValue");
    private final MetaObject setBindValue = new MetaObject("__setBindValue", Map.class);


    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        if (JDBCScope.isInternal()) {
            logger.info("internal jdbc scope. skip trace");
            return;
        }
        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        try {
            String url = getUrl.invoke(target);
            trace.recordRpcName("MYSQL", url);
            trace.recordTerminalEndPoint(url);
            String sql = getSql.invoke(target);
            trace.recordAttribute("PreparedStatement", sql);

            Map bindValue = getBindValue.invoke(target);
            String bindString = toBindVariable(bindValue);
            trace.recordAttribute("BindValue", bindString);

            clean(target);

            trace.record(Annotation.ClientSend);
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
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
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        if (JDBCScope.isInternal()) {
            return;
        }
        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            // TODO 일단 테스트로 실패일경우 종료 아닐경우 resultset fetch까지 계산. fetch count는 옵션으로 빼는게 좋을듯.
            boolean success = InterceptorUtils.isSuccess(result);
            trace.recordAttribute("Success", success);
            if (!success) {
                Throwable th = (Throwable) result;
                trace.recordAttribute("Exception", th.getMessage());
            }
            trace.record(Annotation.ClientRecv);
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

}
