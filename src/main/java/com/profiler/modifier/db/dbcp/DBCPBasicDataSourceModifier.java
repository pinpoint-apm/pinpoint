package com.profiler.modifier.db.dbcp;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;

public class DBCPBasicDataSourceModifier extends AbstractModifier{
	public static byte[] modify(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classFileBuffer) {
		log("DBCPBasicDataSourceModifier modifing");
//		printClassInfo(javassistClassName);
		return changeMethod(classPool,classLoader,javassistClassName,classFileBuffer);
	}
	private static byte[] changeMethod(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateGetConnectionMethod(classPool,cc);
//			updateCloseMethod(classPool,cc);
			byte[] newClassfileBuffer=null;
			newClassfileBuffer= cc.toBytecode();
//			cc.writeFile();
			printClassConvertComplete(javassistClassName);
			return newClassfileBuffer;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private static void updateGetConnectionMethod(ClassPool classPool,CtClass cc) throws Exception {
		CtMethod serviceMethod=cc.getDeclaredMethod("getConnection", null);
		log("*** Changing createStatement method ");
//		serviceMethod.insertBefore(getGetConnectionMethodBeforeInsertCode());
		serviceMethod.insertAfter(getGetConnectionMethodAfterInsertCode());
		
	}
	@SuppressWarnings("unused")
	private static String getGetConnectionMethodBeforeInsertCode() {
		StringBuilder sb=new StringBuilder();
//		sb.append("{");
//		sb.append("System.out.println(\"-----BasicDataSource.getConnection() method is called\");");
//		sb.append("}");
		return sb.toString();
	}
	private static String getGetConnectionMethodAfterInsertCode() {
		StringBuilder sb=new StringBuilder();
		sb.append("{");
//		sb.append("System.out.println(\"-----BasicDataSource.getConnection() method is ended\");");
//		sb.append("System.out.println(\"-----BasicDataSource.getConnection() \"+$0.getClass().getName());");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER+".putConnection("+TomcatProfilerConstant.REQ_DATA_TYPE_DB_GET_CONNECTION+",$0.getUrl());");
		sb.append("}");
		return sb.toString();
	}
	
}
