/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.modifier.spring.beans;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.Modifier;
import com.navercorp.pinpoint.profiler.modifier.spring.beans.interceptor.CreateBeanInstanceInterceptor;
import com.navercorp.pinpoint.profiler.modifier.spring.beans.interceptor.PostProcessorInterceptor;
import com.navercorp.pinpoint.profiler.modifier.spring.beans.interceptor.TargetBeanFilter;

/**
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
 * 
 * @author Jongho Moon <jongho.moon@navercorp.com>
 */
public class AbstractAutowireCapableBeanFactoryModifier extends AbstractModifier {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    
    private final TargetBeanFilter filter;
    private final Modifier modifier;
    
    public static AbstractAutowireCapableBeanFactoryModifier of(ByteCodeInstrumentor byteCodeInstrumentor, ProfilerConfig profilerConfig) {
        Modifier modifier = new BeanMethodModifier(byteCodeInstrumentor);
        return of(byteCodeInstrumentor, profilerConfig, modifier);
    }
    
    public static AbstractAutowireCapableBeanFactoryModifier of(ByteCodeInstrumentor byteCodeInstrumentor, ProfilerConfig profilerConfig, Modifier modifier) {
        TargetBeanFilter filter = TargetBeanFilter.of(profilerConfig);
        
        return new AbstractAutowireCapableBeanFactoryModifier(byteCodeInstrumentor, profilerConfig, filter, modifier);
    }

    public AbstractAutowireCapableBeanFactoryModifier(ByteCodeInstrumentor byteCodeInstrumentor, ProfilerConfig profilerConfig, TargetBeanFilter filter, Modifier modifier) {
        super(byteCodeInstrumentor, profilerConfig);

        this.filter = filter;
        this.modifier = modifier;
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }
        
        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);

            Interceptor createBeanInterceptor = new CreateBeanInstanceInterceptor(byteCodeInstrumentor.getRetransformEventTrigger(), modifier, filter);
            aClass.addInterceptor("createBeanInstance",
                    new String[] { "java.lang.String", "org.springframework.beans.factory.support.RootBeanDefinition", "java.lang.Object[]" },
                    createBeanInterceptor);
            
            Interceptor postProcessorInterceptor = new PostProcessorInterceptor(byteCodeInstrumentor.getRetransformEventTrigger(), modifier, filter);
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
