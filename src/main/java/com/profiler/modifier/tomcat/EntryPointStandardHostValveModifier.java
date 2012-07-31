package com.profiler.modifier.tomcat;

import static com.profiler.config.TomcatProfilerConstant.CLASS_NAME_REQUEST_THRIFT_DTO;
import static com.profiler.config.TomcatProfilerConstant.CLASS_NAME_REQUEST_TRACER;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.modifier.AbstractModifier;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modify org.apache.catalina.core.StandardHostValve class
 * 
 * @author cowboy93, netspider
 * 
 */
public class EntryPointStandardHostValveModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(EntryPointStandardHostValveModifier.class.getName());

	public EntryPointStandardHostValveModifier(ClassPool classPool) {
		super(classPool);
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)){
		    logger.info("Modifing. " + javassistClassName);
        }
		return changeServiceMethod(classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeServiceMethod(ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classfileBuffer));
		try {
			addRequestTracerToCurrentClassLoader(classLoader);

			CtClass cc = classPool.get(javassistClassName);
			CtClass[] params = new CtClass[2];

			params[0] = classPool.getCtClass("org.apache.catalina.connector.Request");
			params[1] = classPool.getCtClass("org.apache.catalina.connector.Response");
			CtMethod serviceMethod = cc.getDeclaredMethod("invoke", params);

			serviceMethod.insertBefore(getInvokeMethodBeforeInsertCode());
			serviceMethod.insertAfter(getInvokeMethodAfterInsertCode());

			CtClass exceptionType = classPool.get("java.lang.Throwable");
			serviceMethod.addCatch(getInvokeMethodCatchInsertCode(), exceptionType);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		return null;
	}

	private String getInvokeMethodBeforeInsertCode() {
		StringBuilder insertCode = new StringBuilder();
		insertCode.append("{");
		insertCode.append("long requestTime=System.currentTimeMillis();");

		insertCode.append("javax.servlet.http.HttpServletRequest tempRequest=(javax.servlet.http.HttpServletRequest)$1;");
		insertCode.append("String requestURL=tempRequest.getRequestURI();");
		insertCode.append("String clientIP=tempRequest.getRemoteAddr();");
		insertCode.append(getParameterValues());
		insertCode.append(CLASS_NAME_REQUEST_TRACER).append(".startTransaction(requestURL,clientIP,requestTime,params);");

		if (logger.isLoggable(Level.FINE)) {
			insertCode.append("System.out.println(\"--- ApplicationFilterChain.doFilter() is started.\");");
		}

		insertCode.append("}");
		return insertCode.toString();
	}

	private StringBuilder getParameterValues() {
		StringBuilder insertCode = new StringBuilder();
		insertCode.append("java.util.Enumeration attrs=tempRequest.getParameterNames();");
		insertCode.append("StringBuilder params=new StringBuilder();");
		insertCode.append("while(attrs.hasMoreElements()) {");
		insertCode.append("String keyString=attrs.nextElement().toString();");

		if (logger.isLoggable(Level.FINE)) {
			insertCode.append("System.out.println(keyString+\"=\"+tempRequest.getParameter(keyString));");
		}

		insertCode.append("Object value=tempRequest.getParameter(keyString);");
		insertCode.append("if(value!=null) {");
		insertCode.append("String valueString=value.toString();");
		insertCode.append("int valueStringLength=valueString.length();");
		insertCode.append("if(valueStringLength>0 && valueStringLength<100) params.append(keyString).append(\"=\").append(valueString).append(\",\");");
		insertCode.append("}}");

		if (logger.isLoggable(Level.FINE)) {
			insertCode.append("System.out.println(params);");
		}

		return insertCode;
	}

	private String getInvokeMethodAfterInsertCode() {
		StringBuilder insertCode = new StringBuilder();
		insertCode.append("{");
		insertCode.append(CLASS_NAME_REQUEST_TRACER).append(".endTransaction();");

		if (logger.isLoggable(Level.FINE)) {
			insertCode.append("System.out.println(\"--- ApplicationFilterChain.doFilter() is ended.\");");
		}

		insertCode.append("}");

		return insertCode.toString();
	}

	private String getInvokeMethodCatchInsertCode() {
		StringBuilder insertCode = new StringBuilder();

		if (logger.isLoggable(Level.FINE)) {
			insertCode.append("{");
			insertCode.append("System.out.println(\"------------------------------------------------\");");
			insertCode.append("System.out.println(\"--- \"+$e.getMessage()+\" is occured !!!\");");
		}

		insertCode.append(CLASS_NAME_REQUEST_TRACER).append(".exceptionTransaction($e);");

		if (logger.isLoggable(Level.FINE)) {
			insertCode.append("System.out.println(\"------------------------------------------------\");");
		}

		insertCode.append("throw $e;");

		if (logger.isLoggable(Level.FINE)) {
			insertCode.append("}");
		}

		return insertCode.toString();
	}

	private void addRequestTracerToCurrentClassLoader(ClassLoader classLoader) {
		try {
			classLoader.loadClass(CLASS_NAME_REQUEST_TRACER);
			classLoader.loadClass(CLASS_NAME_REQUEST_THRIFT_DTO);
			classLoader.loadClass("org.apache.thrift.TBase");
		} catch (Exception e) {
            if(logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
	}
}