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

//    public void setTraceContext(Interceptor interceptor) {
//        // TODO TraceContext를 인터셉터에 바인하는 방안의 추가 개선 필요.
//        if (interceptor instanceof TraceContextSupport) {
//            ((TraceContextSupport) interceptor).setTraceContext(agent.getTraceContext());
//        }
//    }

    public void setServiceType(Interceptor interceptor, ServiceType serviceType) {
        if (interceptor instanceof ServiceTypeSupport) {
            ((ServiceTypeSupport) interceptor).setServiceType(serviceType);
        }
    }

}
