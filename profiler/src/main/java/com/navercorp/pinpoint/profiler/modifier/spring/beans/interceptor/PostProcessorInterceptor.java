package com.nhn.pinpoint.profiler.modifier.spring.beans.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.ClassFileRetransformer;
import com.nhn.pinpoint.profiler.ProfilerException;
import com.nhn.pinpoint.profiler.modifier.Modifier;

public class PostProcessorInterceptor implements SimpleAroundInterceptor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final ClassFileRetransformer retransformer;
    private final Modifier modifier;
    private final TargetBeanFilter filter;

    public PostProcessorInterceptor(ClassFileRetransformer retransformer, Modifier modifier, TargetBeanFilter filter) {
        this.retransformer = retransformer;
        this.modifier = modifier;
        this.filter = filter;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Object bean = result;
        
        if (bean == null) {
            return;
        }
        
        String beanName = (String)args[1];
        Class<? extends Object> clazz = bean.getClass();
        
        if (!filter.isTarget(beanName, clazz)) {
            return;
        }
        
        // TODO 상속받은 메서드도 추적하고 싶다면, parent class들도 찾아서 retransform해야 한다.
         // 우선은 자기 자신의 메서드들만 추적하도록 하고, 추후 요구사항이 생기면 구현한다.
        
        try {
            retransformer.retransform(clazz, modifier);

            if (logger.isInfoEnabled()) {
                logger.info("Retransform " + clazz.getName());
            }
        } catch (ProfilerException e) {
            logger.warn("Fail to retransform: " + clazz.getName(), e);
            return;
        }
        
        filter.addTransformed(clazz);
    }

    @Override
    public void before(Object target, Object[] args) {
        // do nothing
    }
}
