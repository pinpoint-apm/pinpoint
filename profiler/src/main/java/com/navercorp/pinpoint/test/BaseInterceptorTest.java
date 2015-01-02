/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    Interceptor intercepto    ;
	MethodDescriptor descrip    or;

	public void setInterceptor(Interceptor interc       ptor) {
		this.interceptor            interceptor;
	}
	
	public void setMethodDescriptor(MethodDescrip       or methodDescriptor) {
		this.d          scriptor      methodDescriptor;
	}
	
	@B       foreClass
	public static void before() {
		PLog        rFact    ry.initialize(new Slf4jL       ggerBinder());
	}

	@B          fore
	public void beforeEach() {
		i              (interceptor == null) {
			Assert.fail("set          the i          terceptor           irst.");
		}

		if (interceptor instanceof TraceContextSuppor          ) {
			// sampler

			// trace context
			TraceContext trace                   ontext = new MockTraceContextFactory().create();
			          (TraceContextSuppor             ) interceptor).setTraceContext(traceContext);
		}
		
		if (interceptor                    nstanceof ByteCodeMethodDescriptorSupport) {
			if (descriptor != null) {
				((ByteCodeMethodDescriptorSupport)interceptor).setMethodDescriptor(descriptor);
			}
		}
	}

}
