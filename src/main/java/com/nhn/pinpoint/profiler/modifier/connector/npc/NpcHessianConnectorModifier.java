package com.nhn.pinpoint.profiler.modifier.connector.npc;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor.NpcHessianConnectorInterceptor;

/**
 * NPC Hessian connector modifier
 * 
 * <pre>
 * Hooking
 * 
 * public InvocationFuture execute(final String objectName, final String methodName, final Object... params) {
 * 	return this.invoke(objectName, methodName, this.defaultCharset, params);
 * }
 * public InvocationFuture execute(final String objectName, final String methodName, final Charset charset, final Object... params) {
 * 	return this.invoke(objectName, methodName, charset, params);
 * }
 * public InvocationFuture invoke(final String objectName, final String methodName, final Object... params) {
 * 	return this.invoke(objectName, methodName, this.defaultCharset, params);
 * }
 * public InvocationFuture invoke(final String objectName, final String methodName, final Charset charset, final Object... params) {
 * 	NpcMessage message = makeMessage(objectName, methodName, charset, params);
 * 	return this.sendNpcMessage(message);
 * }
 * </pre>
 * 
 * @author netspider
 */
public class NpcHessianConnectorModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(NpcHessianConnectorModifier.class.getName());

	public NpcHessianConnectorModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "com/nhncorp/lucy/npc/connector/NpcHessianConnector";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass connectorClass = byteCodeInstrumentor.getClass(javassistClassName);

			connectorClass.addTraceVariable("__serverAddress", "__setServerAddress", "__getServerAddress", "java.net.InetSocketAddress");

			Interceptor connectInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor.ConnectInterceptor");
			connectorClass.addInterceptor("connect", new String[] { "boolean" }, connectInterceptor);

			// Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor.InvokeInterceptor");
			// connectorClass.addInterceptor("invoke", new String[] { "java.lang.String", "java.lang.String", "java.lang.Object[]" }, interceptor);

			Interceptor interceptor1 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor.InvokeInterceptor");
			connectorClass.addInterceptor("invoke", new String[] { "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]" }, interceptor1);

			// Interceptor interceptor2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor.InvokeInterceptor");
			// connectorClass.addInterceptor("execute", new String[] { "java.lang.String", "java.lang.String", "java.lang.Object[]" }, interceptor2);

			// Interceptor interceptor3 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor.InvokeInterceptor");
			// connectorClass.addInterceptor("execute", new String[] { "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]" }, interceptor3);

			connectorClass.addConstructorInterceptor(new String[] { "java.net.InetSocketAddress", "boolean", "boolean", "boolean", "java.nio.charset.Charset", "long" }, new NpcHessianConnectorInterceptor());
			connectorClass.addConstructorInterceptor(new String[] { "java.net.InetSocketAddress", "com.nhncorp.lucy.npc.connector.ConnectionFactory" }, new NpcHessianConnectorInterceptor());

			return connectorClass.toBytecode();
		} catch (Throwable e) {
			logger.warn(this.getClass().getSimpleName() + " modifier error. Caused:{}", e.getMessage(), e);
			return null;
		}
	}
}