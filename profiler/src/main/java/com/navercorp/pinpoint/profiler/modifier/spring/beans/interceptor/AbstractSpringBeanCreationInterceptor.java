package com.nhn.pinpoint.profiler.modifier.spring.beans.interceptor;

import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.ClassFileRetransformer;
import com.nhn.pinpoint.profiler.ProfilerException;
import com.nhn.pinpoint.profiler.modifier.Modifier;

public abstract class AbstractSpringBeanCreationInterceptor implements SimpleAroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    
    private final ClassFileRetransformer retransformer;
    private final Modifier modifier;
    private final TargetBeanFilter filter;
    
    protected AbstractSpringBeanCreationInterceptor(ClassFileRetransformer retransformer, Modifier modifier, TargetBeanFilter filter) {
        this.retransformer = retransformer;
        this.modifier = modifier;
        this.filter = filter;
    }

    protected final void processBean(String beanName, Object bean) {
        if (bean == null) {
            return;
        }
        
        Class<?> clazz = bean.getClass();
        
        if (!filter.isTarget(beanName, clazz)) {
            return;
        }
        
        // If you want to trace inherited methods, you have to retranform super classes, too.
        
        try {
            retransformer.retransform(clazz, modifier);

            if (logger.isInfoEnabled()) {
                logger.info("Retransform {}", clazz.getName());
            }
        } catch (ProfilerException e) {
            logger.warn("Fail to retransform: {}", clazz.getName(), e);
            return;
        }
        
        filter.addTransformed(clazz);
    }

    @Override
    public final void before(Object target, Object[] args) {
        // do nothing
    }
}
