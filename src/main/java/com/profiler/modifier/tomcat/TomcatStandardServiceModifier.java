package com.profiler.modifier.tomcat;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.Agent;
import com.profiler.modifier.AbstractModifier;

/**
 * When org.apache.catalina.core.StandardService class is loaded in ClassLoader,
 * this class modifies methods.
 * 
 * @author cowboy93, netspider
 * 
 */
public class TomcatStandardServiceModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(TomcatStandardServiceModifier.class.getName());


	public TomcatStandardServiceModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "org/apache/catalina/core/StandardService";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}
		return changeMethod(javassistClassName, classFileBuffer);
	}

	public byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
//            byteCodeInstrumentor.addInterceptor(, "startAgent", null);
			CtMethod startMethod = cc.getDeclaredMethod("start", null);
			startMethod.insertBefore("{" + Agent.FQCN + ".startAgent();" + "}");

			CtMethod stopMethod = cc.getDeclaredMethod("stop", null);
			stopMethod.insertBefore("{" + Agent.FQCN + ".stopAgent();" + "}");

			printClassConvertComplete(javassistClassName);
			return cc.toBytecode();
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return null;
	}
}
