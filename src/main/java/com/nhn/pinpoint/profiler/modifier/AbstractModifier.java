package com.nhn.pinpoint.profiler.modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CtClass;
import javassist.CtMethod;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.ServiceTypeSupport;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.logging.Logger;
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

    /**
     * 대상 클래스의 모든 public 메소드 정보를 반환한다.
     * (이곳에 있는게 맞나?)
     * @param ignoredPrefixes 필터링할 메소드의 prefix를 지정한다. 
     * @return
     */
    public Map<String, String[]> getCandidates(String[] ignoredPrefixes) {
    	Map<String, String[]> candidates = new HashMap<String, String[]>();
    	
    	try {
    		InstrumentClass aClass = byteCodeInstrumentor.getClass(getTargetClass().replace("/", "."));
    		CtMethod[] declaredMethods = aClass.getDeclaredMethods();
    		for (CtMethod m : declaredMethods) {
    			// TODO 필터 좀더 강화해야 함
    			boolean ignored = false;
    			if (m.getModifiers() != javassist.Modifier.PUBLIC) {
    				ignored = true;
    			}
    			for (String ignoredPrefix : ignoredPrefixes) {
    				if (m.getName().startsWith(ignoredPrefix)) {
    					ignored = true;
    					break;
    				}
    			}
    			if (ignored) {
    				continue;
    			}
    			String methodName = m.getName();
    			CtClass[] paramTypes = m.getParameterTypes();
    			List<String> params = new ArrayList<String>(5);
    			for (CtClass c : paramTypes) {
    				params.add(c.getName());
    			}
    			String[] paramArray = new String[params.size()];
    			candidates.put(methodName, params.toArray(paramArray));
    		}
    	} catch (Exception e) {
    		if (logger.isWarnEnabled()) {
    			logger.warn(e.getMessage(), e);
    		}
    	}
    	
    	return candidates;
    }
    
}
