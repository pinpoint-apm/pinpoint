package com.profiler.modifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

public abstract class AbstractModifier {
	public static void printClassInfo(String className) {
		log("Printing Class Info of ["+className+"]");
		try {
			ClassPool pool=ClassPool.getDefault();
//			pool.insertClassPath(new ClassClassPath(currentClass));
			log("try");
			String javaassistClassName = className.replace('/', '.');
			log("replace");
			CtClass cc=pool.get(javaassistClassName);
			log("ClassName:"+javaassistClassName);
			CtConstructor[] constructorList=cc.getConstructors();
			for(CtConstructor cons:constructorList) {
				try {
					String signature=cons.getSignature();
					System.out.println("Constructor signature:"+signature);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			CtMethod[] methodList=cc.getDeclaredMethods();
			for(CtMethod tempMethod:methodList) {
				try {
					String methodName=tempMethod.getLongName();
					log("MethodName:"+methodName);
					CtClass[] params=tempMethod.getParameterTypes();
					if(params.length!=0) {
						int paramsLength=params.length;
						for(int loop=paramsLength-1;loop>0;loop--) {
							log("Param"+loop+":"+params[loop].getName());
						}
					} else {
						log("    No params");
					}
					log("ReturnType="+tempMethod.getReturnType().getName());
				} catch(Exception methodException) {
					log("Exception : "+methodException.getMessage());
				}
			}
		} catch(Exception e) {
			log(e.getMessage());
			e.printStackTrace();
		} 
	}

	public static byte[] addBeforeAfterLogics(ClassPool classPool,String javassistClassName) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			CtMethod[] methods=cc.getDeclaredMethods();
			for(CtMethod method:methods) {
				if(!method.isEmpty() ) {
					String methodName=method.getName();
					CtClass[] params=method.getParameterTypes();
					StringBuilder sb=new StringBuilder();
					if(params.length!=0) {
						int paramsLength=params.length;
						for(int loop=paramsLength-1;loop>0;loop--) {
							sb.append(params[loop].getName()).append(",");
						}
//						sb.substring(0, sb.length()-2);
					}
					method.insertBefore("{System.out.println(\"*****"+javassistClassName+"."+methodName+"("+sb+") is started.\");}");
					method.insertAfter("{System.out.println(\"*****"+javassistClassName+"."+methodName+"("+sb+") is finished.\");}");
				} else {
					log(method.getLongName()+" is empty !!!!!");
				}
			}
			CtConstructor[] constructors=cc.getConstructors();
			for(CtConstructor constructor:constructors) {
				if(!constructor.isEmpty()) {
					CtClass[] params=constructor.getParameterTypes();
					StringBuilder sb=new StringBuilder();
					if(params.length!=0) {
						int paramsLength=params.length;
						for(int loop=paramsLength-1;loop>0;loop--) {
							sb.append(params[loop].getName()).append(",");
						}
//						sb.substring(0, sb.length()-2);
					}
					constructor.insertBefore("{System.out.println(\"*****"+javassistClassName+" Constructor:Param=("+sb+") is started.\");}");
					constructor.insertAfter("{System.out.println(\"*****"+javassistClassName+" Constructor:Param=("+sb+") is finished.\");}");
				} else {
					log(constructor.getLongName()+" is empty !!!!!");
				}
			}
			return cc.toBytecode();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void log(String message) {
		System.out.println("[AbstractModifier] "+message);
	}
	public static void printClassConvertComplete(String javassistClassName) {
		log("@@@ "+javassistClassName+" class is converted !!!");
		
	}
}
