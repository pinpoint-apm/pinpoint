package com.profiler.modifier.tomcat;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.modifier.AbstractModifier;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * When org.apache.catalina.core.StandardService class is loaded in ClassLoader,
 * this class modifies methods.
 * 
 * @author cowboy93, netspider
 * 
 */
public class TomcatConnectorModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(TomcatConnectorModifier.class.getName());

	public TomcatConnectorModifier(ClassPool classPool) {
		super(classPool);
	}
	
	public String getTargetClass() {
		return "org/apache/catalina/connector/Connector";
	}
	
	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)){
		    logger.info("Modifing. " + javassistClassName);
        }
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
            if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		// TODO 변환 실패에 의한 예가 아니면 원본을 반환 해줘야 할까?
		return null;
	}
}