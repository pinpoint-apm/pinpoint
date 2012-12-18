package com.profiler.modifier.tomcat;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.tomcat.interceptors.ConnectorInitializeInterceptor;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.Agent;
import com.profiler.modifier.AbstractModifier;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tomcat connector 정보를 수집하기 위한 modifier
 *
 * @author netspider
 */
public class TomcatConnectorModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(TomcatConnectorModifier.class.getName());

    public TomcatConnectorModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/catalina/connector/Connector";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }
        try {
//            // initialize()할 때 protocol과 port번호를 저장해둔다.
            Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.tomcat.interceptors.ConnectorInitializeInterceptor", new Object[]{agent});
            InstrumentClass aClass = this.byteCodeInstrumentor.getClass(javassistClassName);
            aClass.addInterceptor("initialize", null, interceptor);

            printClassConvertComplete(javassistClassName);

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            return null;
        }
    }


}