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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;

/**
 * @author yinbp[yin-bp@163.com]
 */
public abstract class ElasticsearchBaseOperationInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    protected String className;
    protected ElasticsearchBaseOperationInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext,descriptor);
		this.className = methodDescriptor.getClassName().substring(methodDescriptor.getClassName().lastIndexOf('.')+1);
    }

    protected boolean isTraceMethod(){
		if(methodDescriptor.getMethodName().equals("discover")
				|| methodDescriptor.getMethodName().equals("discoverHost")){
			return false;
		}
		return true;
	}

    @Override
    public void before(Object target, Object[] args) {
		if(!isTraceMethod())
			return ;
        super.before(target,args);
    }


	@Override
	protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {

	}



    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if(!isTraceMethod())
			return ;
        super.after(target,args,result,throwable);

    }
}
