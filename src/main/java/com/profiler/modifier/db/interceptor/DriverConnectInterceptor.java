package com.profiler.modifier.db.interceptor;

import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.modifier.db.util.DatabaseInfo;
import com.profiler.modifier.db.util.JDBCUrlParser;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class DriverConnectInterceptor implements StaticAroundInterceptor {

    private final Logger logger = Logger.getLogger(DriverConnectInterceptor.class.getName());
    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);

    private JDBCUrlParser urlParser = new JDBCUrlParser();

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        JDBCScope.pushScope();

        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        trace.record(Annotation.ClientSend);
        trace.markBeforeTime();

    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        // 여기서는 trace context인지 아닌지 확인하면 안된다. trace 대상 thread가 아닌곳에서 connection이 생성될수 있음.
        JDBCScope.popScope();

        boolean success = InterceptorUtils.isSuccess(result);
        // 여기서는 trace context인지 아닌지 확인하면 안된다. trace 대상 thread가 아닌곳에서 connection이 생성될수 있음.
        DatabaseInfo databaseInfo = createDatabaseInfo((String) args[0]);
        if (success) {
            // 생성이 성공해야 result가 connection임.
            this.setUrl.invoke(result, databaseInfo);
        }

        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.recordRpcName(databaseInfo.getType() + "/" + databaseInfo.getDatabaseId(), databaseInfo.getUrl());
        trace.recordTerminalEndPoint(databaseInfo.getUrl());
        trace.recordAttribute("JDBCConnection", "create");
        if (success) {
            trace.recordAttribute("Success", "true");
        } else {
            Throwable th = (Throwable) result;
            trace.recordAttribute("Success", "false");
            trace.recordAttribute("Exception", th.getMessage());
        }
        trace.record(Annotation.ClientRecv, trace.afterTime());
        trace.traceBlockEnd();
    }

    private DatabaseInfo createDatabaseInfo(String url) {
        DatabaseInfo databaseInfo = urlParser.parse(url);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("parse DatabaseInfo:" + databaseInfo);
        }
        return databaseInfo;
    }


}
