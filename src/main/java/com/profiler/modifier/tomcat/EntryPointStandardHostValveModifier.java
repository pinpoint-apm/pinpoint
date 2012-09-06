package com.profiler.modifier.tomcat;

import static com.profiler.config.TomcatProfilerConstant.CLASS_NAME_REQUEST_THRIFT_DTO;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.bci.InstrumentException;
import javassist.*;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.RequestTracer;

/**
 * Modify org.apache.catalina.core.StandardHostValve class
 * 
 * @author cowboy93, netspider
 * 
 */
public class EntryPointStandardHostValveModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(EntryPointStandardHostValveModifier.class.getName());

	public EntryPointStandardHostValveModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "org/apache/catalina/core/StandardHostValve";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		addRequiredCladdToCurrentClassLoader(classLoader);
        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classFileBuffer));

        try {
            Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.tomcat.interceptors.InvokeMethodInterceptor");
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
            aClass.addInterceptor("invoke", new String[] { "org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response" }, interceptor);
            return aClass.toBytecode();
        } catch (InstrumentException e) {
            logger.log(Level.WARNING, "modify fail Cause:" + e.getMessage(), e);
            return null;
        }
    }

	private void addRequiredCladdToCurrentClassLoader(ClassLoader classLoader) {
		try {
			classLoader.loadClass(RequestTracer.FQCN);
			classLoader.loadClass(CLASS_NAME_REQUEST_THRIFT_DTO);
			classLoader.loadClass("org.apache.thrift.TBase");
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}
}