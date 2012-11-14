package com.profiler.modifier.arcus.interceptors;

import com.profiler.context.AsyncTrace;
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
    private MetaObject asyncTraceId = new MetaObject("__getAsyncTraceId");

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }


        Object asyncId = asyncTraceId.invoke(target);
        if (asyncId == null) {
            logger.fine("asyncId not found id:" + asyncId);
            return;
        }

//        TraceContext traceContext = TraceContext.getTraceContext();
//        GlobalCallTrace globalCallTrace = traceContext.getGlobalCallTrace();
//        AsyncTrace asyncTrace = globalCallTrace.getTraceObject((Integer) asyncId);
//        if (asyncTrace == null) {
//            logger.fine("asyncTrace expired");
//            return;
//        }
        AsyncTrace asyncTrace = (AsyncTrace) asyncId;
        if (asyncTrace.getState() != AsyncTrace.STATE_INIT) {
            // 이미 동작 완료된 상태임.
            return;
        }

        BaseOperationImpl baseOperation = (BaseOperationImpl) target;
        if (!baseOperation.isCancelled()) {
            TimeObject timeObject = (TimeObject) asyncTrace.getAttachObject();
            timeObject.markCancelTime();
        }
    }


}

