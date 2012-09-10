package com.profiler.modifier.db.mysql.interceptors;

import com.mysql.jdbc.PreparedStatement;
import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreparedStatementMethodInterceptor implements StaticAroundInterceptor {

    private final Logger logger = Logger.getLogger(PreparedStatementMethodInterceptor.class.getName());

    private final MetaObject<String> getSql = new MetaObject("__getSql");
    private final MetaObject<String> getUrl = new MetaObject("__getUrl");
    private final MetaObject<List<String>> getBindValue = new MetaObject("__getBindValue");
    private final MetaObject<List<String>> setBindValue = new MetaObject("__setBindValue");


    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
        }
        Trace.traceBlockBegin();
        try {
            String url = getUrl.invoke(target);
            Trace.recordRpcName("mysql", url);

            String sql = getSql.invoke(target);
            Trace.recordAttibute("PreparedStatement", sql);

            List<String> bindValue = getBindValue.invoke(target);
            Trace.recordAttibute("BindValue", bindValue.toString());
            setBindValue.invoke(target, Collections.synchronizedList(new LinkedList<String>()));

            Trace.record(Annotation.ClientSend);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            Trace.traceBlockEnd();
        }

    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
        }

        Trace.traceBlockBegin();
        try {
            // TODO 일단 테스트로 실패일경우 종료 아닐경우 resultset fetch까지 계산. fetch count는 옵션으로 빼는게 좋을듯.
            boolean success = InterceptorUtils.isSuccess(result);
            Trace.recordAttibute("Success", success);
            Trace.record(Annotation.ClientRecv);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Trace.traceBlockEnd();
        }
    }

}
