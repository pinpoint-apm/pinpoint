package com.nhn.pinpoint.profiler.modifier.connector.nimm.interceptor;

import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TargetClassLoader;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.util.MetaObject;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress.Species;

/**
 * target lib = com.nhncorp.lucy.lucy-nimmconnector-2.1.4
 * 
 * @author netspider
 * 
 */
public class NimmInvokerConstructorInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
	private TraceContext traceContext;

	// TODO nimm socket도 수집해야하나?? nimmAddress는 constructor에서 string으로 변환한 값을 들고
	// 있음.
	private MetaObject<String> setNimmAddress = new MetaObject<String>("__setNimmAddress", String.class);

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		if (args[0] instanceof com.nhncorp.lucy.nimm.connector.address.NimmAddress) {
			com.nhncorp.lucy.nimm.connector.address.NimmAddress nimmAddress = (com.nhncorp.lucy.nimm.connector.address.NimmAddress) args[0];

			StringBuilder address = new StringBuilder();
			if (Species.Service.equals(nimmAddress.getSpecies())) {
				address.append("S");
			} else if (Species.Management.equals(nimmAddress.getSpecies())) {
				address.append("M");
			} else {
				address.append("unknown");
			}
			address.append(":");
			address.append(nimmAddress.getDomainId()).append(":");
			address.append(nimmAddress.getIdcId()).append(":");
			address.append(nimmAddress.getServerId()).append(":");
			address.append(nimmAddress.getSocketId());

			setNimmAddress.invoke(target, address.toString());
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