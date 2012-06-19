package com.profiler.modifier.tomcat;

import com.profiler.modifier.AbstractModifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
/**
 * When org.apache.catalina.core.StandardService class is loaded in ClassLoader,
 * this class modifies methods.
 * @author cowboy93
 *
 */
public class TomcatConnectorModifier extends AbstractModifier{
	public static byte[] modify(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classFileBuffer) {
//		printClassInfo(javassistClassName);
		return changeMethod(classPool,classLoader,javassistClassName,classFileBuffer);
	}
	public static byte[] changeMethod(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			
			CtClass param[]=new CtClass[1];
			param[0]=classPool.getCtClass("int");
			CtMethod setPortMethod=cc.getDeclaredMethod("setPort", param);
			setPortMethod.insertBefore("{" +
//					"System.out.println(\"*** setPort() method  *** Port number=\"+$1);" +
					"com.profiler.dto.AgentInfoDTO.portNumberBuffer.append($1).append(\" \");"+
					"}");
			printClassConvertComplete(javassistClassName);
			return cc.toBytecode();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
