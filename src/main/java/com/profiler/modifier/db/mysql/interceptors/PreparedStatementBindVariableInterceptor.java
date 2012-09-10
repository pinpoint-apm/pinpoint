package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreparedStatementBindVariableInterceptor implements StaticAfterInterceptor {
    private final Logger logger = Logger.getLogger(PreparedStatementBindVariableInterceptor.class.getName());

    private final MetaObject<List> getBindValue = new MetaObject<List>("__getBindValue");

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
        }
        List bindList = getBindValue.invoke(target);
        String index = StringUtils.toString(args[0]);
        String value = StringUtils.toString(args[1]);
        bindList.add(index + ":" + value);
    }
}
