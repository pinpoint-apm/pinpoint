package com.profiler.modifier.tomcat;

import com.profiler.config.ProfilerConstant;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.RequestTracer;
import javassist.ByteArrayClassPath;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modify org.apache.catalina.core.StandardHostValve class
 *
 * @author cowboy93, netspider
 */
public class StandardHostValveInvokeInterceptor extends AbstractModifier {

    private final Logger logger = Logger.getLogger(StandardHostValveInvokeInterceptor.class.getName());

    public StandardHostValveInvokeInterceptor(ByteCodeInstrumentor byteCodeInstrumentor) {
        super(byteCodeInstrumentor);
    }

    public String getTargetClass() {
        return "org/apache/catalina/core/StandardHostValve";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classFileBuffer));

        try {
            Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.tomcat.interceptors.StandardHostValveInvokeInterceptor");
            InstrumentClass standardHostValve = byteCodeInstrumentor.getClass(javassistClassName);
            standardHostValve.addInterceptor("invoke", new String[]{"org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response"}, interceptor);
            return standardHostValve.toBytecode();
        } catch (InstrumentException e) {
            logger.log(Level.WARNING, "modify fail. Cause:" + e.getMessage(), e);
            return null;
        }
    }


}