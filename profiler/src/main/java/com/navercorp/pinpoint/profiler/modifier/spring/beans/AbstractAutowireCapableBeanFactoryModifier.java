package com.nhn.pinpoint.profiler.modifier.spring.beans;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.spring.beans.interceptor.AbstractAutowireCapableBeanFactoryInterceptor;

public class AbstractAutowireCapableBeanFactoryModifier extends AbstractModifier {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Interceptor interceptor;
    
    public AbstractAutowireCapableBeanFactoryModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        this(byteCodeInstrumentor, agent, AbstractAutowireCapableBeanFactoryInterceptor.get(agent.getProfilerConfig(), ((DefaultAgent)agent).getRetransformer(), new BeanMethodModifier(byteCodeInstrumentor)));
    }
    
    public AbstractAutowireCapableBeanFactoryModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, Interceptor interceptor) {
        super(byteCodeInstrumentor, agent);
        this.interceptor = interceptor;
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, className);

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(className);
            aClass.addInterceptor("createBean", new String[] { "java.lang.String", "org.springframework.beans.factory.support.RootBeanDefinition", "java.lang.Object[]" }, interceptor);
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
