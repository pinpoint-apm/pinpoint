package com.profiler.modifier.tomcat;

import static com.profiler.config.TomcatProfilerConstant.CLASS_NAME_REQUEST_THRIFT_DTO;

import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.ByteArrayClassPath;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.RequestTracer;

/**
 * Modify org.apache.catalina.core.StandardHostValve class
 * 
 * @author cowboy93, netspider
 * 
 */
public class EntryPointStandardHostValveModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(EntryPointStandardHostValveModifier.class.getName());

	public EntryPointStandardHostValveModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "org/apache/catalina/core/StandardHostValve";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		addRequestTracerToCurrentClassLoader(classLoader);

		System.out.println("\n\n\n\n\n\n");
		System.out.println("EntryPointStandardHostValveModifier=" + classLoader);
		System.out.println("EntryPointStandardHostValveModifier parent=" + classLoader.getParent());

		try {
//			Class.forName("com.profiler.modifier.tomcat.InvokeMethodInterceptor", false, classLoader);
			
			System.out.println(org.apache.catalina.Manager.class.getClassLoader().loadClass("com.profiler.modifier.tomcat.InvokeMethodInterceptor").newInstance().getClass().getClassLoader());
			
			System.out.println(classLoader.loadClass("com.profiler.modifier.tomcat.InvokeMethodInterceptor").newInstance().getClass().getClassLoader());
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("\n\n\n\n\n\n");

		this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classFileBuffer));

		InstrumentClass aClass = this.byteCodeInstrumentor.getClass(javassistClassName);

		try {
			aClass.addInterceptor("invoke", new String[] { "org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response" }, (Interceptor) org.apache.catalina.Manager.class.getClassLoader().loadClass("com.profiler.modifier.tomcat.InvokeMethodInterceptor").newInstance());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return aClass.toBytecode();
	}

	private void addRequestTracerToCurrentClassLoader(ClassLoader classLoader) {
		try {
			classLoader.loadClass(RequestTracer.FQCN);
			classLoader.loadClass(CLASS_NAME_REQUEST_THRIFT_DTO);
			classLoader.loadClass("org.apache.thrift.TBase");
			// classLoader.loadClass("com.profiler.modifier.tomcat.InvokeMethodInterceptor");
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}
}