package com.nhn.pinpoint.profiler.modifier;

import java.util.*;

import com.nhn.pinpoint.profiler.interceptor.bci.Method;
import com.nhn.pinpoint.profiler.util.JavaAssistUtils;
import javassist.CtClass;
import javassist.CtMethod;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractModifier implements Modifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

}
