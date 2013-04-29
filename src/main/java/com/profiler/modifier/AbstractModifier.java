package com.profiler.modifier;

import com.profiler.logging.Logger;

import com.profiler.common.ServiceType;
import com.profiler.interceptor.ServiceTypeSupport;

import com.profiler.Agent;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.logging.LoggerFactory;

public abstract class AbstractModifier implements Modifier {

    private final Logger logger = LoggerFactory.getLogger(AbstractModifier.class.getName());

    protected final ByteCodeInstrumentor byteCodeInstrumentor;
    protected final Agent agent;

    public Agent getAgent() {
        return agent;
    }

    public AbstractModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        this.byteCodeInstrumentor = byteCodeInstrumentor;
        this.agent = agent;
    }

    public void printClassConvertComplete(String javassistClassName) {
        if (logger.isInfoEnabled()) {
            logger.info(javassistClassName + " class is converted.");
        }
    }

    public void setServiceType(Interceptor interceptor, ServiceType serviceType) {
        if (interceptor instanceof ServiceTypeSupport) {
            ((ServiceTypeSupport) interceptor).setServiceType(serviceType);
        }
    }

}
