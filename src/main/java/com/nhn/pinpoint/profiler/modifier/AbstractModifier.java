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

    /**
     * 대상 클래스의 모든 public 메소드 정보를 반환한다.
     * (이곳에 있는게 맞나?)
     * @param ignoredPrefixes 필터링할 메소드의 prefix를 지정한다. 
     * @return
     */
    public List<Method> getCandidates(String[] ignoredPrefixes) {

    	try {
    		InstrumentClass aClass = byteCodeInstrumentor.getClass(getTargetClass().replace("/", "."));
    		CtMethod[] declaredMethods = aClass.getDeclaredMethods();

            List<Method> candidates = new ArrayList<Method>(declaredMethods.length);
            forStart:
    		for (CtMethod m : declaredMethods) {
    			// TODO 필터 좀더 강화해야 함
    			if (m.getModifiers() != javassist.Modifier.PUBLIC) {
    				continue;
    			}
    			for (String ignoredPrefix : ignoredPrefixes) {
    				if (m.getName().startsWith(ignoredPrefix)) {
    					continue forStart;
    				}
    			}
    			String methodName = m.getName();
    			CtClass[] paramTypes = m.getParameterTypes();
                String[] parameterType = JavaAssistUtils.getParameterType(paramTypes);
                candidates.add(new Method(methodName, parameterType));
    		}
            return candidates;
    	} catch (Exception e) {
    		if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
    		}
    	}
    	return Collections.emptyList();
    }
    
}
