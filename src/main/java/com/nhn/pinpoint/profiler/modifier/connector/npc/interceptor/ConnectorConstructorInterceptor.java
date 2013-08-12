package com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor;

import java.net.InetSocketAddress;

import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.util.MetaObject;
import com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector;
import com.nhncorp.lucy.npc.connector.NpcConnectorOption;

/**
 * based on NPC client 1.5.18
 * 
 * @author netspider
 * 
 */
public class ConnectorConstructorInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final Logger logger = LoggerFactory.getLogger(ConnectorConstructorInterceptor.class.getName());
	private final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
	private TraceContext traceContext;

	private MetaObject<InetSocketAddress> setServerAddress = new MetaObject<InetSocketAddress>("__setServerAddress", InetSocketAddress.class);

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		if (target instanceof KeepAliveNpcHessianConnector) {
			/*
			 * com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector.
			 * KeepAliveNpcHessianConnector(InetSocketAddress, long, long,
			 * Charset)
			 */
			if (args.length == 4) {
				if (args[0] instanceof InetSocketAddress) {
					setServerAddress.invoke(target, (InetSocketAddress) args[0]);
				}
			} else if (args.length == 1) {
				if (args[0] instanceof NpcConnectorOption) {
					NpcConnectorOption option = (NpcConnectorOption) args[0];
					InetSocketAddress address = option.getAddress();
					setServerAddress.invoke(target, address);
				}
			}
		} else {
			if (args[0] instanceof NpcConnectorOption) {
				NpcConnectorOption option = (NpcConnectorOption) args[0];
				InetSocketAddress address = option.getAddress();
				setServerAddress.invoke(target, address);
			}
		}
	}

	@Override
	public void after(Object target, Object[] args, Object result) {

	}

	@Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		traceContext.cacheApi(descriptor);
	}

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;
	}
}