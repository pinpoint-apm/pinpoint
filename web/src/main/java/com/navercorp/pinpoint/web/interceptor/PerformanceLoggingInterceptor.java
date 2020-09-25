package com.navercorp.pinpoint.web.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Aspect
public class PerformanceLoggingInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long slow;

    public PerformanceLoggingInterceptor(long slow) {
        this.slow = slow;
    }

    @Around("within(@org.springframework.stereotype.Controller *)")
    public Object logging(ProceedingJoinPoint joinPoint) throws Throwable {

        final long start = System.currentTimeMillis();
        Throwable capture = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable th) {
            capture = th;
            throw th;
        } finally {
            final long time = System.currentTimeMillis() - start;
            if (capture != null) {
                warnLog(joinPoint, time, capture);
            } else if (time > slow) {
                warnLog(joinPoint, time, capture);
            } else {
                if (logger.isDebugEnabled()) {
                    final String className = joinPoint.getTarget().getClass().getSimpleName();
                    final String methodName = joinPoint.getSignature().getName();
                    logger.debug("{}.{} execution time:{}ms param:{}", className, methodName, time, joinPoint.getArgs());
                }
            }
        }
    }

    private void warnLog(ProceedingJoinPoint joinPoint, long time, Throwable throwable) {
        final Logger logger = getLogger(joinPoint);
        if (logger.isWarnEnabled()) {
            final String className = joinPoint.getTarget().getClass().getSimpleName();
            final String methodName = joinPoint.getSignature().getName();
            if (throwable != null) {
                logger.warn("{}.{} execution time:{}ms param:{}", className, methodName, time, joinPoint.getArgs(), throwable);
            } else {
                logger.warn("{}.{} execution time:{}ms param:{}", className, methodName, time, joinPoint.getArgs());
            }
        }
    }

    private Logger getLogger(ProceedingJoinPoint joinPoint) {
        return logger;
//        return LoggerFactory.getLogger(joinPoint.getTarget().getClass());
    }
}
