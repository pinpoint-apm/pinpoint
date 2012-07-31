package com.profiler.modifier.tomcat;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

/**
 * When org.apache.catalina.core.StandardService class is loaded in ClassLoader,
 * this class modifies methods.
 * 
 * @author cowboy93, netspider
 * 
 */
public class TomcatConnectorModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(TomcatConnectorModifier.class);

	public TomcatConnectorModifier(ClassPool classPool) {
		super(classPool);
	}
	
	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("Modifing. %s", javassistClassName);
		return changeMethod(javassistClassName, classFileBuffer);
	}

	public byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			CtClass param[] = new CtClass[1];
			param[0] = classPool.getCtClass("int");
			CtMethod setPortMethod = cc.getDeclaredMethod("setPort", param);
			
			setPortMethod.insertBefore("{ com.profiler.dto.AgentInfoDTO.portNumberBuffer.append($1).append(\" \"); }");

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		// TODO 변환 실패에 의한 예가 아니면 원본을 반환 해줘야 할까?
		return null;
	}
}