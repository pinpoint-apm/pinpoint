package com.nhn.pinpoint.profiler.modifier.connector.npc;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor.InvokeInterceptor;
import com.nhn.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NPC Hessian connector modifier
 * 
 * based on NPC client 1.5.18
 * 
 * @author netspider
 */
public class KeepAliveNpcHessianConnectorModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public KeepAliveNpcHessianConnectorModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "com/nhncorp/lucy/npc/connector/KeepAliveNpcHessianConnector";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass connectorClass = byteCodeInstrumentor.getClass(javassistClassName);

			// trace variables
			connectorClass.addTraceVariable("_serverAddress", "__setServerAddress", "__getServerAddress", "java.net.InetSocketAddress");

			// package constructor
			Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor.ConnectorConstructorInterceptor");
			connectorClass.addConstructorInterceptor(new String[] { "com.nhncorp.lucy.npc.connector.NpcConnectorOption" }, constructorInterceptor);

			// public constructor
			Interceptor constructorInterceptor2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor.ConnectorConstructorInterceptor");
			connectorClass.addConstructorInterceptor(new String[] { "java.net.InetSocketAddress", "long", "long", "java.nio.charset.Charset" }, constructorInterceptor2);

			// initializing connector
			Interceptor initializeConnectorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor.InitializeConnectorInterceptor");
			connectorClass.addInterceptor("initializeConnector", null, initializeConnectorInterceptor);

			// invokeImpl
			Interceptor invokeImplInterceptor = new MethodInterceptor();
			connectorClass.addInterceptor("invokeImpl", new String[] { "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]" }, invokeImplInterceptor);

			// invoke
			Interceptor invokeInterceptor = new InvokeInterceptor();
			connectorClass.addInterceptor("invoke", new String[] { "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]" }, invokeInterceptor);

			return connectorClass.toBytecode();
		} catch (Throwable e) {
			logger.warn(this.getClass().getSimpleName() + " modifier error. Caused:{}", e.getMessage(), e);
			return null;
		}
	}
}