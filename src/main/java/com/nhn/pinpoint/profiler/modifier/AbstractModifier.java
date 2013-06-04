package com.nhn.pinpoint.profiler.modifier;

import com.nhn.pinpoint.profiler.logging.Logger;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.interceptor.ServiceTypeSupport;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;

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
