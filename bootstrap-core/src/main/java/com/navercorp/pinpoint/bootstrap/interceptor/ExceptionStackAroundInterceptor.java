package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.SpanThrowable;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * The exception stack interceptor.
 *
 * @author IluckySi
 * @version 2.1.0
 * @since 2020/08/23
 */
public abstract class ExceptionStackAroundInterceptor implements AroundInterceptor {

    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    public Throwable processThrowable(ProfilerConfig config, Object target,  Object result, Throwable throwable) {
        return processThrowable(config, target, null, result, throwable);
    }

    // Process Throwable
    public Throwable processThrowable(ProfilerConfig config, Object target, Object[] args, Object result, Throwable throwable) {
        SpanThrowable spanThrowable = null;
        try {
            if(throwable == null) return null;
            spanThrowable = new SpanThrowable();

            boolean exceptionStackTraceEnable = config.getExceptionStackTraceEnable();
            if (exceptionStackTraceEnable) {
                int exceptionStackTraceLine = config.getExceptionStackTraceLine();
                StackTraceElement[] stackTraceElementArray = throwable.getStackTrace();
                int length = stackTraceElementArray.length;
                if (exceptionStackTraceLine != -1) {
                    length = exceptionStackTraceLine < length ? exceptionStackTraceLine : length;
                }

                StackTraceElement[] newStackTraceElementArray = new StackTraceElement[length];
                for (int i = 0; i < length; i++) {
                    StackTraceElement stackTraceElement = stackTraceElementArray[i];
                    if (stackTraceElement != null) {
                        newStackTraceElementArray[i] = stackTraceElement;
                    }
                }

                spanThrowable.setStackTraceElements(newStackTraceElementArray);
            } else {
                spanThrowable.setStackTraceElements(new StackTraceElement[0]);
            }

            spanThrowable.setMessage(throwable.getMessage());
            spanThrowable.setCause(throwable.getCause());
            return spanThrowable;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("THROWABLR error. Caused:{}", e.getMessage(), e);
            }
            return throwable;
        }
    }
}
