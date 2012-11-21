package com.profiler.modifier.bloc.handler;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 */
public class HTTPHandlerModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(HTTPHandlerModifier.class.getName());

	public HTTPHandlerModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "com/nhncorp/lucy/bloc/handler/HTTPHandler$BlocAdapter";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.bloc.handler.interceptors.ExecuteMethodInterceptor");
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
			aClass.addInterceptor("execute", new String[] { "external.org.apache.coyote.Request", "external.org.apache.coyote.Response" }, interceptor);
			return aClass.toBytecode();
		} catch (InstrumentException e) {
			// TODO log
			return null;
		}
	}
}