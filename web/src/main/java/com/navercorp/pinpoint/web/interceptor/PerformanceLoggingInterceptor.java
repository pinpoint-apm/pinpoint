/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.interceptor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import static com.navercorp.pinpoint.common.server.log.Makers.AOP;


@Aspect
public class PerformanceLoggingInterceptor {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final long slow;

    public PerformanceLoggingInterceptor(int threadhold) {
        this.slow = threadhold;
    }


    @Around("@within(org.springframework.web.bind.annotation.RestController)")
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
                errorLog(joinPoint, time, capture);
            } else if (time > slow) {
                slowLog(joinPoint, time);
            } else {
                debugLog(joinPoint, time);
            }
        }
    }

    private void debugLog(ProceedingJoinPoint joinPoint, long time) {
        final Logger logger = getLogger(joinPoint);
        if (logger.isDebugEnabled(AOP)) {
            final String className = joinPoint.getTarget().getClass().getSimpleName();
            final String methodName = joinPoint.getSignature().getName();
            logger.debug(AOP, "[AOP] {}.{} execution time:{}ms param:{}", className, methodName, time, joinPoint.getArgs());
        }
    }

    private void errorLog(ProceedingJoinPoint joinPoint, long time, Throwable throwable) {
        final Logger logger = getLogger(joinPoint);
        if (logger.isInfoEnabled(AOP)) {
            final String className = joinPoint.getTarget().getClass().getSimpleName();
            final String methodName = joinPoint.getSignature().getName();
            logger.info(AOP, "[AOP] {} {}.{} execution time:{}ms param:{} error:{}", "ERROR", className, methodName, time, joinPoint.getArgs(), throwable.getMessage());
        }
    }

    private void slowLog(ProceedingJoinPoint joinPoint, long time) {
        final Logger logger = getLogger(joinPoint);
        if (logger.isInfoEnabled(AOP)) {
            final String className = joinPoint.getTarget().getClass().getSimpleName();
            final String methodName = joinPoint.getSignature().getName();
            logger.info(AOP, "[AOP] {} {}.{} execution time:{}ms param:{}", "SLOW", className, methodName, time, joinPoint.getArgs());
        }
    }

    private Logger getLogger(ProceedingJoinPoint joinPoint) {
        return logger;
//        return LogManager.getLogger(joinPoint.getTarget().getClass());
    }
}
