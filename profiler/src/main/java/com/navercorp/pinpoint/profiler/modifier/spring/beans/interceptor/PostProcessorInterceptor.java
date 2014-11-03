package com.nhn.pinpoint.profiler.modifier.spring.beans.interceptor;

import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

import com.nhn.pinpoint.profiler.ClassFileRetransformer;
import com.nhn.pinpoint.profiler.modifier.Modifier;

public class PostProcessorInterceptor extends AbstractSpringBeanCreationInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    
    public PostProcessorInterceptor(ClassFileRetransformer retransformer, Modifier modifier, TargetBeanFilter filter) {
        super(retransformer, modifier, filter);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        try {
            Object bean = result;
            String beanName = (String)args[1];
            
            processBean(beanName, bean);
        } catch (Throwable t) {
            logger.warn("Unexpected exception", t);
        }
    }
}
