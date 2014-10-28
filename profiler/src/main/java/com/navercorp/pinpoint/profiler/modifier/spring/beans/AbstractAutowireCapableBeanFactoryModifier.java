package com.nhn.pinpoint.profiler.modifier.spring.beans;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.ClassFileRetransformer;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.Modifier;
import com.nhn.pinpoint.profiler.modifier.spring.beans.interceptor.CreateBeanInstanceInterceptor;
import com.nhn.pinpoint.profiler.modifier.spring.beans.interceptor.PostProcessorInterceptor;
import com.nhn.pinpoint.profiler.modifier.spring.beans.interceptor.TargetBeanFilter;

/*
 * Spring beans are created by AbstractAutowireCapableBeanFactory#createBean(String, RootBeanDefinition, Object[]).
 * If we intercept the return value of this method, we can check every beans created by Spring bean container.
 * 
 * There is one exception. Someone can create bean object from outside of Spring bean container and register it through DefaultSingletonBeanRegistry#registerSingleton(String, Object).
 * But such beans are not in our interest.
 * 
 * One more thing we must consider is proxy.
 * Pinpoint profiler could miss a target bean wrapped by a proxy, because the proxy class is different from the wrapped bean class(usually proxy class is subclass of the original class).
 * 
 * There are two points where once created bean is replaced with other bean(maybe proxy) before createBean(String, RootBeanDefinition, Object[]) returns.
 * 
 * 1. When a bean is acquired by resolveBeforeInstantiation(String, RootBeanDefinition),
 *    original bean returned by applyBeanPostProcessorsBeforeInstantiation(Class<?>, String) could be replaced by applyBeanPostProcessorsAfterInitialization(Object, String).
 *
 * 2. When a bean is created by doCreateBean(), 
 *    original bean returned by createBeanInstance(String, RootBeanDefinition, Object[]) is passed to initializeBean(String, Object, RootBeanDefinition). 
 *    initializeBean(String, Object, RootBeanDefinition) then invokes applyBeanPostProcessorsBeforeInstantiation(Object, String) and applyBeanPostProcessorsAfterInitialization(Object, String) which could replace the bean with other object.   
 * 
 * 
 * Therefore we have to intercept lower level method which creates original bean object rather than createBean(String, RootBeanDefinition, Object[]).
 * 
 * 1. createBeanInstance(String, RootBeanDefinition, Object[])
 * 2. applyBeanPostProcessorsBeforeInstantiation(Class<?>, String)
 * 
 * 
 * 
 * Spring source code related to bean creation remains almost same.
 * We have checked Spring versions from 2.5.6 to 4.1.0.
 */
public class AbstractAutowireCapableBeanFactoryModifier extends AbstractModifier {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final ClassFileRetransformer retransformer;
    private final TargetBeanFilter filter;
    private final Modifier modifier;
    
    public static AbstractAutowireCapableBeanFactoryModifier of(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, ClassFileRetransformer retransformer) {
        Modifier modifier = new BeanMethodModifier(byteCodeInstrumentor);
        return of(byteCodeInstrumentor, agent, retransformer, modifier);
    }
    
    public static AbstractAutowireCapableBeanFactoryModifier of(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, ClassFileRetransformer retransformer, Modifier modifier) {
        TargetBeanFilter filter = TargetBeanFilter.of(agent.getProfilerConfig());
        
        return new AbstractAutowireCapableBeanFactoryModifier(byteCodeInstrumentor, agent, retransformer, filter, modifier);
    }

    public AbstractAutowireCapableBeanFactoryModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, ClassFileRetransformer retransformer, TargetBeanFilter filter, Modifier modifier) {
        super(byteCodeInstrumentor, agent);

        this.retransformer = retransformer;
        this.filter = filter;
        this.modifier = modifier;
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }
        
        byteCodeInstrumentor.checkLibrary(classLoader, className);

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(className);

            Interceptor createBeanInterceptor = new CreateBeanInstanceInterceptor(retransformer, modifier, filter);
            aClass.addInterceptor("createBeanInstance",
                    new String[] { "java.lang.String", "org.springframework.beans.factory.support.RootBeanDefinition", "java.lang.Object[]" },
                    createBeanInterceptor);
            
            Interceptor postProcessorInterceptor = new PostProcessorInterceptor(retransformer, modifier, filter);
            aClass.addInterceptor("applyBeanPostProcessorsBeforeInstantiation", new String[] { "java.lang.Class", "java.lang.String" }, postProcessorInterceptor);
            
            
            return aClass.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("AbstractAutowireCapableBeanFactoryModifier failed. Caused:", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getTargetClass() {
        return "org/springframework/beans/factory/support/AbstractAutowireCapableBeanFactory";
    }
}
