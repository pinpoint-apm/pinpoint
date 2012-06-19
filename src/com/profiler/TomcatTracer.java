package com.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class TomcatTracer implements ClassFileTransformer {
	protected String agentArgString = "";
	protected Instrumentation instrumentation;
	ClassPool classPool;
	public static void premain(String agentArgs, Instrumentation inst) {
//		TomcatProfiler profiler = new TomcatProfiler(agentArgs, inst);
		new TomcatTracer(agentArgs, inst);
	}

	public TomcatTracer(String agentArgs, Instrumentation inst) {
		agentArgString = agentArgs;
		instrumentation = inst;
		instrumentation.addTransformer(this);
		classPool = ClassPool.getDefault();
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
//		System.out.println(className);
		String javassistClassName = className.replace('/', '.');
		if(javassistClassName.startsWith("javax.servlet.") ||
				javassistClassName.startsWith("org.apache.tomcat") ) {
			if(!javassistClassName.startsWith("org.apache.tomcat.util")) {
				byte []result=changeCode(javassistClassName,classfileBuffer);
				if(result!=null) return result;
			}
		} else {
//			System.out.println("*** "+javassistClassName);
		}
		
		return null;
	}
	private byte[] changeCode(String javassistClassName,byte[] classfileBuffer) {
		boolean insertFlag=false;
//		System.out.println("*** "+javassistClassName);
		
//		System.out.println("*** ClassPool OK");
		classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classfileBuffer));
//		System.out.println("*** insertClassPath OK");
		try {
			if(classPool==null) {
				System.out.println("NULL");
				classPool = ClassPool.getDefault();
			}
			CtClass cc = classPool.get(javassistClassName);
//			System.out.println("%%% ClassLoader="+cc.getClass().getClassLoader());
			CtMethod[] methodList=cc.getDeclaredMethods();
			for(CtMethod method:methodList) {
//				System.out.println(method.getLongName());
				String methodName=method.getLongName();
				if(!method.isEmpty()) {
					System.out.println("***inserted instrument code at "+methodName);
//					method.insertBefore("{ System.out.println(\"### "+method.getName()+"  is called. URL=\"+$1+\" \");}");
					method.insertBefore("{ System.out.println(\"--- "+methodName+"  is called. \");}");
					insertFlag=true;
				}
			}
			if(insertFlag) {
				byte[] newClassfileBuffer = cc.toBytecode();
				System.out.println("@@@"+javassistClassName+"class's new Buffer generated !!!");
				
				return newClassfileBuffer;
			}
		} catch(Exception e) {
//			e.printStackTrace();
			System.err.println("!!!!! "+e.getMessage());
		}
		return null;
	}
}
