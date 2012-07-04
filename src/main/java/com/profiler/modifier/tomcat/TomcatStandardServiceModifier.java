package com.profiler.modifier.tomcat;

import static com.profiler.config.TomcatProfilerConstant.CLASS_NAME_AGENT_STATE_MANAGER;

import com.profiler.modifier.AbstractModifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * When org.apache.catalina.core.StandardService class is loaded in ClassLoader,
 * this class modifies methods.
 * 
 * @author cowboy93
 * 
 */
public class TomcatStandardServiceModifier extends AbstractModifier {
	public static byte[] modify(ClassPool classPool, ClassLoader classLoader,
			String javassistClassName, byte[] classFileBuffer) {
		// printClassInfo(javassistClassName);
		return changeMethod(classPool, classLoader, javassistClassName,
				classFileBuffer);
	}

	public static byte[] changeMethod(ClassPool classPool,
			ClassLoader classLoader, String javassistClassName,
			byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			CtMethod startMethod = cc.getDeclaredMethod("start", null);
			startMethod
					.insertBefore("{"
							+ "System.out.println(\"*** Start TomcatProfiler JVMStat Thread ***\");"
							+ CLASS_NAME_AGENT_STATE_MANAGER
							+ ".startJVMTraceThread();" + "}");
			CtMethod stopMethod = cc.getDeclaredMethod("stop", null);
			stopMethod
					.insertBefore("{"
							+ "System.out.println(\"*** TomcatProfiler send JVM is stopped info ***\");"
							+ CLASS_NAME_AGENT_STATE_MANAGER
							+ ".sendJVMStoppedInfo();" + "}");

			printClassConvertComplete(javassistClassName);
			return cc.toBytecode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
