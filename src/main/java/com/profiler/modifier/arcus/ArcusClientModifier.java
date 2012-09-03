package com.profiler.modifier.arcus;

import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.context.Trace;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
public class ArcusClientModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(ArcusClientModifier.class.getName());

	public ArcusClientModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "net/spy/memcached/protocol/BaseOperationImpl";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		try {
			classLoader.loadClass("net.spy.memcached.ops.OperationState");

			CtClass cc = classPool.get(javassistClassName);
			CtClass[] params1 = new CtClass[1];
			params1[0] = classPool.getCtClass("net.spy.memcached.ops.OperationState");
			CtMethod transitionStateMethod = cc.getDeclaredMethod("transitionState", params1);

			StringBuilder code = new StringBuilder();
			code.append("{");
			
//			code.append("System.out.println($1);");
//			code.append("System.out.println(\"Change state \" + state + \" -> \" + newState);");
//			code.append("System.out.println(handlingNode);");
//			code.append("System.out.println(\"cmd=\" + ((cmd == null) ? null : new String(cmd.array())));");
//			code.append("System.out.println(Thread.currentThread().getId());");
//			code.append("System.out.println(Thread.currentThread().getName());");
//			code.append("System.out.println(\"\");");
//			code.append("System.out.println(\"\");");
//			code.append("System.out.println(\"\");");
			
			code.append("if (newState == net.spy.memcached.ops.OperationState.READING) {");
			code.append("	java.net.SocketAddress socketAddress = handlingNode.getSocketAddress();");
			code.append("	if (socketAddress instanceof java.net.InetSocketAddress) {");
			code.append("		java.net.InetSocketAddress addr = (java.net.InetSocketAddress) handlingNode.getSocketAddress();");
			code.append("		com.profiler.context.Trace.recordServerAddr(addr.getHostName(), addr.getPort());");
			code.append("	}");
			code.append("	com.profiler.context.Trace.recordRpcName(\"arcus\", ((cmd == null) ? \"UNKNOWN\" : new String(cmd.array())));");
			code.append("	System.out.println(\"CS\");");
			code.append("	com.profiler.context.Trace.record(new com.profiler.context.Annotation.ClientSend());");
			code.append("} else if (newState == net.spy.memcached.ops.OperationState.COMPLETE) {");
			code.append("	System.out.println(\"CR\");");
			code.append("	com.profiler.context.Trace.record(new com.profiler.context.Annotation.ClientRecv());");
			code.append("}");
			
			code.append("}");
			transitionStateMethod.insertBefore(code.toString());
			
			return cc.toBytecode();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}