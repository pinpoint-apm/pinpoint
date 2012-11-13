package com.profiler.modifier.arcus.interceptors;

import com.profiler.context.AsyncTrace;
import com.profiler.context.GlobalCallTrace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;
import net.spy.memcached.protocol.BaseOperationImpl;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class BaseOperationCancelInterceptor implements StaticBeforeInterceptor {
    private final Logger logger = Logger.getLogger(BaseOperationCancelInterceptor.class.getName());
    private MetaObject asyncTraceId = new MetaObject<Integer>("__getAsyncTraceId", null);

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }

        TraceContext traceContext = TraceContext.getTraceContext();
        GlobalCallTrace globalCallTrace = traceContext.getGlobalCallTrace();
        Object asyncId = asyncTraceId.invoke(target);
        if (asyncId == null) {
            logger.info("asyncId not found");
            return;
        }
        AsyncTrace asyncTrace = globalCallTrace.removeTraceObject((Integer) asyncId);
        BaseOperationImpl baseOperation = (BaseOperationImpl) target;
        if (!baseOperation.isCancelled()) {
            TimeObject timeObject = (TimeObject) asyncTrace.getAttachObject();
            timeObject.markCancelTime();
        }
    }


}

