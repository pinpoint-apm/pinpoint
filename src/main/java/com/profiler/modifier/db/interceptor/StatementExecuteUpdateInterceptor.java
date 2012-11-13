package com.profiler.modifier.db.interceptor;

import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * protected int executeUpdate(String sql, boolean isBatch, boolean returnGeneratedKeys)
 *
 * @author netspider
 */
public class StatementExecuteUpdateInterceptor implements StaticAroundInterceptor {

    private final Logger logger = Logger.getLogger(StatementExecuteUpdateInterceptor.class.getName());

    private final MetaObject<String> getUrl = new MetaObject<String>("__getUrl");

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
        trace.markBeforeTime();
        try {
            if (args.length > 0) {
                String url = (String) this.getUrl.invoke(target);
                trace.recordRpcName("MYSQL", url);
                trace.recordAttribute("Query", url);
                trace.recordTerminalEndPoint(url);
            } else {
                trace.recordRpcName("MYSQL", "UNKNOWN");
                trace.recordTerminalEndPoint("UNKNOWN");
            }

            trace.record(Annotation.ClientSend);

        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
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

        // TODO 결과, 수행시간을.알수 있어야 될듯.
        trace.record(Annotation.ClientRecv, trace.afterTime());
        trace.traceBlockEnd();
    }
}
