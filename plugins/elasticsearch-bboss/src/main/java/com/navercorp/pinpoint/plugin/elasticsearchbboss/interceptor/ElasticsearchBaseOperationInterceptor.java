/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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

package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author yinbp[yin-bp@163.com]
 */
public abstract class ElasticsearchBaseOperationInterceptor implements AroundInterceptor {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final MethodDescriptor methodDescriptor;
    protected final TraceContext traceContext;
    protected String className;


	protected String mergeParameterVariableNameDescription(StringBuilder sb ,String[] parameterType, String[] variableName) {
		if (parameterType == null && variableName == null) {
			return "()";
		} else if (variableName != null && parameterType != null) {
			if (parameterType.length != variableName.length) {
				throw new IllegalArgumentException("args size not equal");
			} else if (parameterType.length == 0) {
				return "()";
			} else {

				sb.append('(');
				int end = parameterType.length - 1;

				for(int i = 0; i < parameterType.length; ++i) {
					sb.append(parameterType[i]);
					sb.append('_');
					sb.append(variableName[i]);
					if (i < end) {
						sb.append(",");
					}
				}

				sb.append(')');
				return sb.toString();
			}
		} else {
			throw new IllegalArgumentException("invalid null pair parameterType:" + Arrays.toString(parameterType) + ", variableName:" + Arrays.toString(variableName));
		}
	}


	public String convertParams(Object[] args){

		if(args != null && args.length > 0){
			if(args.length == 1){
				return String.valueOf(args[0]);
			}
			StringBuilder builder = new StringBuilder();
			int i = 0;
			for(Object arg:args) {

				if(i > 0) {
					builder.append(",");

				}
				convertInner(  arg,  builder);
				i ++;
			}
			return builder.toString();
		}
		return null;
	}

	private void convertInner(Object arg,StringBuilder builder){
		boolean isArray = arg != null && arg.getClass().isArray();
		if(!isArray) {
			builder.append(arg);
		}
		else {
			builder.append("[");
			for (int i = 0; i < Array.getLength(arg); i ++) {
				if (i > 0) {
					builder.append(",");

				}
				convertInner(Array.get(arg,i),  builder);
			}
			builder.append("]");
		}
	}

    protected ElasticsearchBaseOperationInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (descriptor == null) {
            throw new NullPointerException("descriptor must not be null");
        }
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;
		this.className = methodDescriptor.getClassName().substring(methodDescriptor.getClassName().lastIndexOf('.')+1);
    }


    protected Trace createTrace(Object target, Object[] args) {
          return traceContext.newTraceObject();
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
        if (isDebug) {
            logBeforeInterceptor(target, args);
        }

        prepareBeforeTrace(target, args);

		Trace trace = traceContext.currentTraceObject();

		if (trace == null) {
			return;
		}


		try {
			final SpanEventRecorder recorder = trace.traceBlockBegin();
			doInBeforeTrace(recorder, target, args);
		} catch (Throwable th) {
			if (logger.isWarnEnabled()) {
				logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
			}
		}
    }



    protected void logBeforeInterceptor(Object target, Object[] args) {
        logger.beforeInterceptor(target, args);
    }

    protected void prepareBeforeTrace(Object target, Object[] args) {

    }


    protected void doInBeforeTrace(final SpanEventRecorder recorder, final Object target, final Object[] args){

	}

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if(!isTraceMethod())
			return ;
        if (isDebug) {
            logAfterInterceptor(target, args, result, throwable);
        }

        prepareAfterTrace(target, args, result, throwable);

		final Trace trace = traceContext.currentRawTraceObject();
		if (trace == null) {
			return;
		}
		try {
			final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
			doInAfterTrace(recorder, target, args, result, throwable);
		} catch (Throwable th) {
			if (logger.isWarnEnabled()) {
				logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
			}
		} finally {
			trace.traceBlockEnd();
		}

    }



    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, args, result, throwable);
    }

    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
    }

    protected abstract void doInAfterTrace(final SpanEventRecorder recorder, final Object target, final Object[] args, final Object result, Throwable throwable);
    protected MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    protected TraceContext getTraceContext() {
        return traceContext;
    }






}
