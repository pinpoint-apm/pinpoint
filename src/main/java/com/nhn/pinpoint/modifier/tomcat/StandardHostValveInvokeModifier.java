package com.nhn.pinpoint.modifier.tomcat;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.logging.Logger;
import com.nhn.pinpoint.logging.LoggerFactory;

import com.nhn.pinpoint.Agent;
import com.nhn.pinpoint.interceptor.Interceptor;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.modifier.AbstractModifier;

/**
 * Modify org.apache.catalina.core.StandardHostValve class
 *
 * @author cowboy93, netspider
 */
public class StandardHostValveInvokeModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(StandardHostValveInvokeModifier.class.getName());

    public StandardHostValveInvokeModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/catalina/core/StandardHostValve";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
            Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "StandardHostValveInvokeInterceptor");

            InstrumentClass standardHostValve = byteCodeInstrumentor.getClass(javassistClassName);
            standardHostValve.addInterceptor("invoke", new String[]{"org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response"}, interceptor);
            return standardHostValve.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("modify fail. Cause:" + e.getMessage(), e);
            return null;
        }
    }


}