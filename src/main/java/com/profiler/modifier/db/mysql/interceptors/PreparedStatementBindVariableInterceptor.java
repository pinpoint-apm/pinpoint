package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.util.MetaObject;
import com.profiler.util.NumberUtils;
import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreparedStatementBindVariableInterceptor implements StaticAfterInterceptor {
    private final Logger logger = Logger.getLogger(PreparedStatementBindVariableInterceptor.class.getName());

    private final MetaObject<Map> getBindValue = new MetaObject<Map>("__getBindValue");

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
        }
        Map bindList = getBindValue.invoke(target);
        Integer index = NumberUtils.toInteger(args[0]);
        if(index == null) {
            // 어딘가 잘못됨.
            return;
        }
        String value = StringUtils.toString(args[1]);
        bindList.put(index, value);
    }
}
