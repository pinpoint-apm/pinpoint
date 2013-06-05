package com.nhn.pinpoint.profiler.modifier.arcus;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.Type;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;


/**
 * @author netspider
 */
public class MemcachedClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(MemcachedClientModifier.class.getName());

    public MemcachedClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/spy/memcached/MemcachedClient";
    }
    
    /*
     * TODO 
     * 설정으로 annotation on/off
     * future interceptor
     * 쓰레드별 그룹핑
     * 
     */
    public Map<String, String[]> getCandidates() {
    	Map<String, String[]> candidates = new HashMap<String, String[]>();
    	
    	try {
    		InstrumentClass aClass = byteCodeInstrumentor.getClass(getTargetClass().replace("/", "."));
    		CtMethod[] declaredMethods = aClass.getDeclaredMethods();
    		for (CtMethod m : declaredMethods) {
    			// TODO 정규식 쓸 수 있도록 변경한 후, 유틸리티로 빼내자.
    			if (m.getModifiers() != Modifier.PUBLIC || m.getName().startsWith("_")) {
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

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            aClass.addTraceVariable("__serviceCode", "__setServiceCode", "__getServiceCode", "java.lang.String");

            Interceptor addOpInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.arcus.interceptors.AddOpInterceptor");
            aClass.addInterceptor("addOp", new String[]{"java.lang.String", "net.spy.memcached.ops.Operation"}, addOpInterceptor, Type.before);

            for (Entry<String, String[]> e : getCandidates().entrySet()) {
            	Interceptor apiInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.arcus.interceptors.ApiInterceptor");
            	aClass.addInterceptor(e.getKey(), e.getValue(), apiInterceptor, Type.around);
            }
            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn( e.getMessage(), e);
            }
            return null;
        }
    }
}