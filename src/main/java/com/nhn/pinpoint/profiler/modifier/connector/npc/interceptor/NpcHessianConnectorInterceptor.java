package com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor;

import java.net.InetSocketAddress;

import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.util.MetaObject;

public class NpcHessianConnectorInterceptor implements SimpleAroundInterceptor {

	private final Logger logger = LoggerFactory.getLogger(NpcHessianConnectorInterceptor.class.getName());
	private final boolean isDebug = logger.isDebugEnabled();

	private final MetaObject<InetSocketAddress> setServerAddress = new MetaObject<InetSocketAddress>("__setServerAddress", InetSocketAddress.class);

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		if (args[0] instanceof InetSocketAddress) {
			setServerAddress.invoke(target, (InetSocketAddress) args[0]);
		}
	}

	@Override
	public void after(Object target, Object[] args, Object result) {

	}
}