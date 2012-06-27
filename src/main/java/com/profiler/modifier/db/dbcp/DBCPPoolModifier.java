package com.profiler.modifier.db.dbcp;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;

public class DBCPPoolModifier extends AbstractModifier{
	public static byte[] modify(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classFileBuffer) {
		log("DBCPPoolModifier(PoolingDataSource$PoolGuardConnectionWrapper) modifing");
//		printClassInfo(javassistClassName);
		return changeMethod(classPool,classLoader,javassistClassName,classFileBuffer);
	}
	private static byte[] changeMethod(ClassPool classPool,ClassLoader classLoader,String javassistClassName,byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
//			updateGetConnectionMethod(classPool,cc);
			updateCloseMethod(classPool,cc);
/*
			CtMethod[] methods=cc.getDeclaredMethods();
			for(CtMethod method:methods) {
//				System.out.println(method.getLongName());
				if(!method.isEmpty() ) {
					String methodName=method.getName();
					if(methodName.equals("getConnection")) {
//							&& !methodName.startsWith("version")) {
						method.insertBefore("{System.out.println(\"-----DBCP "+method.getLongName()+" is started.\");}");
						method.insertAfter("{System.out.println(\"-----DBCP "+method.getLongName()+" is finished.\");}");
						System.out.println(method.getLongName());
					}
				} else {
					log(method.getLongName()+" is empty !!!!!");
				}
			}
*/
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
//		sb.append("System.out.println(\"-----PoolingDataSource$PoolGuardConnectionWrapper.close() method is called\");");
//		sb.append("}");
		return sb.toString();
	}
	
	private static String getCloseMethodAfterInsertCode() {
		StringBuilder sb=new StringBuilder();
		sb.append("{");
//		sb.append("System.out.println(\"-----PoolingDataSource$PoolGuardConnectionWrapper.close() method is ended\");");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER+".put("+TomcatProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION+");");
		sb.append("}");
		return sb.toString();
	}
}
