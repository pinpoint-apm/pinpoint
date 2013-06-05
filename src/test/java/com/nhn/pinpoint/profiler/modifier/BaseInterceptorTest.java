package com.nhn.pinpoint.profiler.modifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;

import com.nhn.pinpoint.profiler.context.DefaultTraceContext;
import com.nhn.pinpoint.profiler.context.MockTraceContextFactory;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.sampler.Sampler;

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
		LoggerFactory.initialize(new Slf4jLoggerBinder());
	}

	@Before
	public void beforeEach() {
		if (interceptor == null) {
			Assert.fail("set the interceptor first.");
		}

		if (interceptor instanceof TraceContextSupport) {
			// sampler
			Sampler sampler = mock(Sampler.class);
			when(sampler.isSampling()).thenReturn(true);

			// trace context
			TraceContext traceContext = new MockTraceContextFactory().create();
			((DefaultTraceContext) traceContext).setSampler(sampler);
			((TraceContextSupport) interceptor).setTraceContext(traceContext);
		}
		
		if (interceptor instanceof ByteCodeMethodDescriptorSupport) {
			if (descriptor != null) {
				((ByteCodeMethodDescriptorSupport)interceptor).setMethodDescriptor(descriptor);
			}
		}
	}

}
