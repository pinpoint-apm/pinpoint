package com.profiler.modifier.db.mssql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;

public class MSSQLConnectionModifier extends AbstractModifier{
	public static byte[] modify(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classFileBuffer) {
		log("MSSQLConnectionModifier modifing");
//		printClassInfo(javassistClassName);
		return changeMethods(classPool,classLoader,javassistClassName,classFileBuffer);
	}
	private static byte[] changeMethods(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateCreateStatementMethod(classPool,cc);
			updateCloseMethod(classPool,cc);
			byte[] newClassfileBuffer = cc.toBytecode();
//			cc.writeFile();
			printClassConvertComplete(javassistClassName);
			return newClassfileBuffer;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void updateCreateStatementMethod(ClassPool classPool,CtClass cc) throws Exception {
		CtClass[] params=new CtClass[2];
		params[0]=classPool.getCtClass("int");
		params[1]=classPool.getCtClass("int");
		CtMethod serviceMethod=cc.getDeclaredMethod("createStatement", params);
		log("*** Changing createStatement method ");
//		serviceMethod.insertBefore(getCreateStatementMethodBeforeInsertCode());
		serviceMethod.insertAfter(getCreateStatementMethodAfterInsertCode());
	}
	@SuppressWarnings("unused")
	private static String getCreateStatementMethodBeforeInsertCode() {
		StringBuilder sb=new StringBuilder();
//		sb.append("{");
//		sb.append("System.out.println(\"-----ConnectionJDBC2.createStatement() method is called\");");
//		sb.append("}");
		return sb.toString();
	}
	private static String getCreateStatementMethodAfterInsertCode() {
		StringBuilder sb=new StringBuilder();
		sb.append("{");
//		sb.append("System.out.println(\"-----ConnectionJDBC2.createStatement() method is ended\");");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER+".put("+TomcatProfilerConstant.REQ_DATA_TYPE_DB_CREATE_STATEMENT+");");
		sb.append("}");
		return sb.toString();
	}
	private static void updateCloseMethod(ClassPool classPool,CtClass cc) throws Exception {
		CtMethod serviceMethod=cc.getDeclaredMethod("close", null);
		log("*** Changing close method ");
//		serviceMethod.insertBefore(getCloseMethodBeforeInsertCode());
		serviceMethod.insertAfter(getCloseMethodAfterInsertCode());
	}
	@SuppressWarnings("unused")
	private static String getCloseMethodBeforeInsertCode() {
		StringBuilder sb=new StringBuilder();
//		sb.append("{");
//		sb.append("System.out.println(\"-----ConnectionJDBC2.close() method is called\");");
//		sb.append("}");
		return sb.toString();
	}
	private static String getCloseMethodAfterInsertCode() {
		StringBuilder sb=new StringBuilder();
		sb.append("{");
//		sb.append("System.out.println(\"-----ConnectionJDBC2.close() method is ended\");");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER+".put("+TomcatProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION+");");
		sb.append("}");
		return sb.toString();
	}
}
