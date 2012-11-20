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
    private MetaObject getAsyncTrace = new MetaObject("__getAsyncTrace");

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }

        AsyncTrace asyncTrace = (AsyncTrace) getAsyncTrace.invoke(target);
        if (asyncTrace == null) {
            logger.fine("asyncTrace not found ");
            return;
        }

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

