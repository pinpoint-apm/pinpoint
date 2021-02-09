package com.navercorp.pinpoint.threadx;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.threadx.interceptor.TaskCallInterceptor;
import com.navercorp.pinpoint.threadx.interceptor.ThreadPoolExecutorSubmitInterceptor;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.List;

public class ThreadPoolPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private MatchableTransformTemplate transformTemplate;





    @Override
    public void setup(ProfilerPluginSetupContext context) {
        logger.info("zhangyinhao,loaddingThreadPoolPlugin...");
        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig(context.getConfig());
        logger.info("zhangyinhao,loaddingThreadPoolPlugin...enable={}",threadPoolConfig.isEnable());
        if(!threadPoolConfig.isEnable()){
            logger.debug("ThreadPoolConfig 不可用");
            return;
        }

        logger.debug("ThreadPoolConfig config={}",threadPoolConfig);

        String sdkPagkage = "com.binpo.pinpoint.sdk.concurrent";

        addAsyncExecutionInterceptorTask(Matchers.newClassNameMatcher(sdkPagkage+".AsyncCallable$1"),"call");
        addAsyncExecutionInterceptorTask(Matchers.newClassNameMatcher(sdkPagkage+".AsyncRunnable$1"),"run");

        addAsyncTaskExecutor(sdkPagkage+".AsyncThreadPoolExecutor");

        addAsyncTaskExecutor(sdkPagkage+".AsyncScheduledThreadPoolExecutor");

        addAsyncTaskExecutor(sdkPagkage+".AsyncForkJoinPool");

    }



    private void addAsyncExecutionInterceptorTask(final Matcher matcher,final String methodName) {
        logger.info("zhangyinhao,loaddingThreadPoolPlugin...,matcher={},methodName={}",matcher,methodName);
        transformTemplate.transform(matcher, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class);
                final InstrumentMethod callMethod = target.getDeclaredMethod(methodName);
                logger.info("zhangyinhao,loaddingThreadPoolPlugin...,callMethod={},methodName={}",callMethod,methodName);
                if (callMethod != null) {
                    callMethod.addInterceptor(TaskCallInterceptor.class);
                }
                return target.toBytecode();
            }
        });
    }




    private void addAsyncTaskExecutor(final String className) {
        transformTemplate.transform(className, AsyncRunTransformCallback.class);
    }

    public static class AsyncRunTransformCallback implements TransformCallback {
        private final PLogger logger = PLoggerFactory.getLogger(getClass());
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("execute"))) {
                logger.info("zhangyinhao,loaddingThreadPoolPlugin...doInTransform,execute");
                m.addScopedInterceptor(ThreadPoolExecutorSubmitInterceptor.class, ThreadPoolConstants.THREAD_POOL_EXECUTOR_SCOPE);
            }
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("submit"))) {
                logger.info("zhangyinhao,loaddingThreadPoolPlugin...doInTransform,submit");
                m.addScopedInterceptor(ThreadPoolExecutorSubmitInterceptor.class, ThreadPoolConstants.THREAD_POOL_EXECUTOR_SCOPE);
            }
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("schedule"))) {
                logger.info("zhangyinhao,loaddingThreadPoolPlugin...doInTransform,schedule");
                m.addScopedInterceptor(ThreadPoolExecutorSubmitInterceptor.class, ThreadPoolConstants.THREAD_POOL_EXECUTOR_SCOPE);
            }
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("scheduleAtFixedRate"))) {
                logger.info("zhangyinhao,loaddingThreadPoolPlugin...doInTransform,scheduleAtFixedRate");
                m.addScopedInterceptor(ThreadPoolExecutorSubmitInterceptor.class, ThreadPoolConstants.THREAD_POOL_EXECUTOR_SCOPE);
            }
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("scheduleWithFixedDelay"))) {
                logger.info("zhangyinhao,loaddingThreadPoolPlugin...doInTransform,scheduleWithFixedDelay");
                m.addScopedInterceptor(ThreadPoolExecutorSubmitInterceptor.class, ThreadPoolConstants.THREAD_POOL_EXECUTOR_SCOPE);
            }
            return target.toBytecode();
        }
    }



    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
