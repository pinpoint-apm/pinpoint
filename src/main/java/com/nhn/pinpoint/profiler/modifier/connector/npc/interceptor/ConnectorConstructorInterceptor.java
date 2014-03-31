package com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor;

import java.net.InetSocketAddress;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.*;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.util.MetaObject;
import com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector;
import com.nhncorp.lucy.npc.connector.NpcConnectorOption;

/**
 * based on NPC client 1.5.18
 * 
 * @author netspider
 * 
 */
public class ConnectorConstructorInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
	private TraceContext traceContext;

	private final MetaObject<InetSocketAddress> setServerAddress = new MetaObject<InetSocketAddress>("__setServerAddress", InetSocketAddress.class);

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		InetSocketAddress serverAddress = null;

		if (target instanceof KeepAliveNpcHessianConnector) {
			/*
			 * com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector.
			 * KeepAliveNpcHessianConnector(InetSocketAddress, long, long,
			 * Charset)
			 */
			if (args.length == 4) {
				if (args[0] instanceof InetSocketAddress) {
					serverAddress = (InetSocketAddress) args[0];
				}
			} else if (args.length == 1) {
				if (args[0] instanceof NpcConnectorOption) {
					NpcConnectorOption option = (NpcConnectorOption) args[0];
					serverAddress = option.getAddress();
				}
			}
		} else {
			if (args[0] instanceof NpcConnectorOption) {
				NpcConnectorOption option = (NpcConnectorOption) args[0];
				serverAddress = option.getAddress();
			}
		}

		setServerAddress.invoke(target, serverAddress);

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

		trace.traceBlockBegin();
		trace.markBeforeTime();
		trace.recordServiceType(ServiceType.NPC_CLIENT);

		if (serverAddress != null) {
			int port = serverAddress.getPort();
			String endPoint = serverAddress.getHostName() + ((port > 0) ? ":" + port : "");
			trace.recordDestinationId(endPoint);
		} else {
			// destination id가 없으면 안되기 때문에 unknown으로 지정.
			trace.recordDestinationId("unknown");
		}
	}

	@Override
	public void after(Object target, Object[] args, Object result) {
		if (isDebug) {
			logger.afterInterceptor(target, args);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

		try {
			trace.recordApi(descriptor);
			trace.recordException(result);
			trace.markAfterTime();
		} finally {
			trace.traceBlockEnd();
		}
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