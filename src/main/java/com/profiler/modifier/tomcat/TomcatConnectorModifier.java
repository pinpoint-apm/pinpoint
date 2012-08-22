package com.profiler.modifier.tomcat;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.Agent;
import com.profiler.modifier.AbstractModifier;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tomcat connector 정보를 수집하기 위한 modifier
 * 
 * @author netspider
 *
 */
public class TomcatConnectorModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(TomcatConnectorModifier.class.getName());

	public TomcatConnectorModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "org/apache/catalina/connector/Connector";
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

			// initialize()할 때 protocol과 port번호를 저장해둔다.
			CtMethod initializeMethod = cc.getDeclaredMethod("initialize", null);
			initializeMethod.insertAfter("{" + Agent.FQCN + ".getInstance().getServerInfo().addConnector(getProtocol(), getPort()); }");

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		// TODO 변환 실패에 의한 예가 아니면 원본을 반환 해줘야 할까?
		return null;
	}
}