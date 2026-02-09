package com.navercorp.pinpoint.collector.aop;

import com.navercorp.pinpoint.collector.manage.HandlerManager;
import com.navercorp.pinpoint.common.server.io.ServerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import java.util.Objects;

import static com.navercorp.pinpoint.collector.aop.AvailabilityHandlerAop.ORDER;
import static com.navercorp.pinpoint.common.server.log.Makers.AOP;


@Aspect
@Order(ORDER)
public class AvailabilityHandlerAop {
    public static final int ORDER = 10000;

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled(AOP);

    private final HandlerManager handlerManager;

    public AvailabilityHandlerAop(HandlerManager handlerManager) {
        this.handlerManager = Objects.requireNonNull(handlerManager, "handlerManager");
    }

    @Pointcut("@within(org.springframework.stereotype.Service) && target(com.navercorp.pinpoint.collector.handler.SimpleHandler+)")
    public void simpleHandlerPointcut() {
    }


    @Around("simpleHandlerPointcut() && execution(public void handleSimple(..))")
    public Object aroundHandleSimple(ProceedingJoinPoint joinPoint) throws Throwable {
        if (isDebug) {
            logger.debug(AOP, "[AOP] Check availability {}", joinPoint.toShortString());
        }
        if (!checkAvailable()) {
            logger.debug(AOP, "[AOP] Handler is disabled. Skipping send message {}.", joinPoint.getSignature());
            return null;
        }
        return joinPoint.proceed();
    }


    @Pointcut("@within(org.springframework.stereotype.Service) && target(com.navercorp.pinpoint.collector.handler.RequestResponseHandler+)")
    public void requestResponseHandlerPointcut() {
    }

    @Around("requestResponseHandlerPointcut()" +
            " && execution(public void handleRequest(..))" +
            " && args(com.navercorp.pinpoint.common.server.io.ServerRequest, serverResponse)"
    )
    public Object aroundHandleRequest(ProceedingJoinPoint joinPoint, ServerResponse<?> serverResponse) throws Throwable {
        if (isDebug) {
            logger.debug(AOP, "[AOP] Check availability {}", joinPoint.toShortString());
        }

        if (!checkAvailable()) {
            logger.debug(AOP, "[AOP] Handler is disabled. Skipping send message {}.", joinPoint.getSignature());
            serverResponse.finish();
            return null;
        }
        return joinPoint.proceed();
    }


    private boolean checkAvailable() {
        return handlerManager.isEnable();
    }
}
