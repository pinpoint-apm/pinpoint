package com.profiler.modifier.tomcat;

import static com.profiler.config.TomcatProfilerConstant.CLASS_NAME_REQUEST_TRACER;
import static com.profiler.config.TomcatProfilerConstant.CLASS_NAME_REQUEST_THRIFT_DTO;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.modifier.AbstractModifier;
/** 
 * Modify org.apache.catalina.core.StandardHostValve class
 * @author cowboy93
 *
 */
public class EntryPointStandardHostValveModifier extends AbstractModifier {
	public static byte[] modify(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classFileBuffer) {
		log("EntryPointModifier.modifyStandardHostValve()");
//		printClassInfo(javassistClassName);
		return changeServiceMethod(classPool,classLoader,javassistClassName,classFileBuffer);
	}
	private static byte[] changeServiceMethod(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classfileBuffer) {
		classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classfileBuffer));
		try {
			addRequestTracerToCurrentClassLoader(classLoader);
//			log("Class loader="+classPool.getClassLoader().toString());
			CtClass cc = classPool.get(javassistClassName);
			CtClass[] params=new CtClass[2];
			
			params[0]=classPool.getCtClass("org.apache.catalina.connector.Request");
			params[1]=classPool.getCtClass("org.apache.catalina.connector.Response");
			CtMethod serviceMethod=cc.getDeclaredMethod("invoke", params);
			log("*** Changing invoke method ");
			serviceMethod.insertBefore(getInvokeMethodBeforeInsertCode());
			serviceMethod.insertAfter(getInvokeMethodAfterInsertCode());
			
			CtClass exceptionType = classPool.get("java.lang.Throwable");
			//CtClass exceptionType = classPool.get("java.lang.Exception");
			serviceMethod.addCatch(getInvokeMethodCatchInsertCode(), exceptionType);
			
//			cc.stopPruning(true);
//			cc.toClass(classLoader,classLoader.getClass().getProtectionDomain());
//			cc.stopPruning(false);
			
			byte[] newClassfileBuffer = cc.toBytecode();
//			cc.writeFile();
			printClassConvertComplete(javassistClassName);
			return newClassfileBuffer;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String getInvokeMethodBeforeInsertCode() {
		StringBuilder insertCode=new StringBuilder();
		insertCode.append("{");
		insertCode.append("long requestTime=System.currentTimeMillis();");
		
		insertCode.append("javax.servlet.http.HttpServletRequest tempRequest=(javax.servlet.http.HttpServletRequest)$1;");
		insertCode.append("String requestURL=tempRequest.getRequestURI();");
		insertCode.append("String clientIP=tempRequest.getRemoteAddr();");
		insertCode.append(getParameterValues());
		insertCode.append(CLASS_NAME_REQUEST_TRACER).append(".startTransaction(requestURL,clientIP,requestTime,params);");
		
//		insertCode.append("System.out.println(\"--- ApplicationFilterChain.doFilter() is started.\");");
		insertCode.append("}");
		return insertCode.toString();
	}
	private static StringBuilder getParameterValues() {
		StringBuilder insertCode=new StringBuilder();
		insertCode.append("java.util.Enumeration attrs=tempRequest.getParameterNames();");
		insertCode.append("StringBuilder params=new StringBuilder();");
		insertCode.append("while(attrs.hasMoreElements()) {");
		insertCode.append("String keyString=attrs.nextElement().toString();");
//		insertCode.append("System.out.println(key+\"=\"+tempRequest.getParameter(key.toString()));");
		insertCode.append("Object value=tempRequest.getParameter(keyString);");
		insertCode.append("if(value!=null) {");
		insertCode.append("String valueString=value.toString();");
		insertCode.append("int valueStringLength=valueString.length();");
		insertCode.append("if(valueStringLength>0 && valueStringLength<100) params.append(keyString).append(\"=\").append(valueString).append(\",\");");
		insertCode.append("}}");
//		insertCode.append("System.out.println(params);");
		return insertCode;
	}
	private static String getInvokeMethodAfterInsertCode() {
		StringBuilder insertCode=new StringBuilder();
		insertCode.append("{");
		insertCode.append(CLASS_NAME_REQUEST_TRACER).append(".endTransaction();");
//		insertCode.append("System.out.println(\"--- ApplicationFilterChain.doFilter() is ended.\");");
		insertCode.append("}");
		return insertCode.toString();
	}
	private static String getInvokeMethodCatchInsertCode() {
		StringBuilder insertCode=new StringBuilder();
//		insertCode.append("{");
//		insertCode.append("System.out.println(\"------------------------------------------------\");");
//		insertCode.append("System.out.println(\"--- \"+$e.getMessage()+\" is occured !!!\");");
		insertCode.append(CLASS_NAME_REQUEST_TRACER).append(".exceptionTransaction($e);");
//		insertCode.append("System.out.println(\"------------------------------------------------\");");
		insertCode.append("throw $e;");
//		insertCode.append("}");
		return insertCode.toString();
	}
	private static void addRequestTracerToCurrentClassLoader(ClassLoader classLoader) {
		try {
			classLoader.loadClass(CLASS_NAME_REQUEST_TRACER);
			classLoader.loadClass(CLASS_NAME_REQUEST_THRIFT_DTO);
			classLoader.loadClass("org.apache.thrift.TBase");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
