/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.elasticsearchbboss.ElasticsearchConstants;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ParallelWorkerRunInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

	public ParallelWorkerRunInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
		super(traceContext, methodDescriptor);
	}

	@Override
	protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
		// do nothing
	}

	@Override
	protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
		recorder.recordServiceType(ElasticsearchConstants.ELASTICSEARCH);
		recorder.recordApi(methodDescriptor);
		recorder.recordException(throwable);
	}
}
