package com.profiler.modifier.tomcat;

import com.profiler.util.ByteCodeUtil;
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

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		if (logger.isDebugEnabled()) {
            ByteCodeUtil.printClassInfo(classPool, javassistClassName);
		}
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	public byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			CtClass param[] = new CtClass[1];
			param[0] = classPool.getCtClass("int");
			CtMethod setPortMethod = cc.getDeclaredMethod("setPort", param);
			setPortMethod.insertBefore("{" +
			// "System.out.println(\"*** setPort() method  *** Port number=\"+$1);"
			// +
					"com.profiler.dto.AgentInfoDTO.portNumberBuffer.append($1).append(\" \");" + "}");

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}