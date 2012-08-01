package com.profiler.modifier.tomcat;

import static com.profiler.config.TomcatProfilerConstant.CLASS_NAME_AGENT_STATE_MANAGER;
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
public class TomcatStandardServiceModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(TomcatStandardServiceModifier.class.getName());

	public TomcatStandardServiceModifier(ClassPool classPool) {
		super(classPool);
	}
	
	public String getTargetClass() {
		return "org/apache/catalina/core/StandardService";
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

			CtMethod startMethod = cc.getDeclaredMethod("start", null);
			startMethod.insertBefore("{" + "System.out.println(\"*** Start TomcatProfiler JVMStat Thread ***\");" + CLASS_NAME_AGENT_STATE_MANAGER + ".startJVMTraceThread();" + "}");

			CtMethod stopMethod = cc.getDeclaredMethod("stop", null);
			stopMethod.insertBefore("{" + "System.out.println(\"*** TomcatProfiler send JVM is stopped info ***\");" + CLASS_NAME_AGENT_STATE_MANAGER + ".sendJVMStoppedInfo();" + "}");

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
