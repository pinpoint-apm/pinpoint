package com.nhn.pinpoint.profiler.modifier.tomcat.interceptor;

import org.apache.catalina.util.ServerInfo;

import com.nhn.pinpoint.bootstrap.interceptor.LifeCycleEventListener;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class StandardServiceStartInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private LifeCycleEventListener lifeCycleEventListener;

    public StandardServiceStartInterceptor(LifeCycleEventListener lifeCycleEventListener) {
        this.lifeCycleEventListener = lifeCycleEventListener;
    }

    @Override
    public void before(Object target, Object[] args) {
        // Do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        String tomcatInfo = ServerInfo.getServerInfo();
        logger.info("Tomcat Version : {}", tomcatInfo);
        // if (!InterceptorUtils.isSuccess(result)) {
        // return;
        // }
        lifeCycleEventListener.start();
    }
}
