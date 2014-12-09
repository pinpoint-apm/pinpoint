package com.navercorp.pinpoint.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinder;

public class BaseInterceptorTest {

	Interceptor interceptor;
	MethodDescriptor descriptor;

	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public void setMethodDescriptor(MethodDescriptor methodDescriptor) {
		this.descriptor = methodDescriptor;
	}
	
	@BeforeClass
	public static void before() {
		PLoggerFactory.initialize(new Slf4jLoggerBinder());
	}

	@Before
	public void beforeEach() {
		if (interceptor == null) {
			Assert.fail("set the interceptor first.");
		}

		if (interceptor instanceof TraceContextSupport) {
			// sampler

			// trace context
			TraceContext traceContext = new MockTraceContextFactory().create();
			((TraceContextSupport) interceptor).setTraceContext(traceContext);
		}
		
		if (interceptor instanceof ByteCodeMethodDescriptorSupport) {
			if (descriptor != null) {
				((ByteCodeMethodDescriptorSupport)interceptor).setMethodDescriptor(descriptor);
			}
		}
	}

}
