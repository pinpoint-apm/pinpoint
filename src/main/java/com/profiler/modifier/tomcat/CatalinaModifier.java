package com.profiler.modifier.tomcat;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.Agent;
import com.profiler.modifier.AbstractModifier;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tomcat startup정보를 HIPPO서버로 전송하는 코드를 호출하기위한 modifier
 * 
 * @author netspider
 * 
 */
public class CatalinaModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(CatalinaModifier.class.getName());

	public CatalinaModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "org/apache/catalina/startup/Catalina";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}
		return changeMethod(javassistClassName, classFileBuffer);
	}

	public byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			/**
			 * Tomcat startup완료되면 Catalina.await()을 호출하고 stop되기를 기다린다. 이 때
			 * await하기 전에 서버가 시작되면서 수집된 WAS정보를 HIPPO 서버로 전송한다.
			 */
			CtMethod initializeMethod = cc.getDeclaredMethod("await", null);
			initializeMethod.insertBefore("{" + Agent.FQCN + ".getInstance().sendStartupInfo(); }");

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