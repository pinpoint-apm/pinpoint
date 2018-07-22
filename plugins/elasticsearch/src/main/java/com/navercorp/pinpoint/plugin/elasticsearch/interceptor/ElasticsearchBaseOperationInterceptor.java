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

package com.navercorp.pinpoint.plugin.elasticsearch.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

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
			StringBuilder builder = new StringBuilder();
			for(Object arg:args) {
				boolean isArray = arg != null && arg.getClass().isArray();


				if(builder.length() > 0) {
					builder.append(",");

				}
				if(!isArray) {
					builder.append(arg);
				}
				else{
					convertArray(  arg,  builder);
				}
			}
			return builder.toString();
		}
		return null;
	}

	public void convertArray(Object arg,StringBuilder builder){
		builder.append("[");
		Object[] fields = (Object[])arg;
		boolean isfirst = true;
		for(Object f:fields){
			if(isfirst){
				isfirst = false;
			}
			else{
				builder.append(",");

			}
			builder.append(f);
		}
		builder.append("]");
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

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logBeforeInterceptor(target, args);
        }

        prepareBeforeTrace(target, args);

        Trace trace = traceContext.currentTraceObject();
        boolean newTrace = false;
        if (trace == null) {
            trace = createTrace(  target,   args);
            newTrace = true;
            doInBeforeTrace(trace.getSpanRecorder(), target, args, newTrace);

        }
        else {

            try {
                final SpanEventRecorder recorder = trace.traceBlockBegin();
                doInBeforeTrace(recorder, target, args, newTrace);
            } catch (Throwable th) {
                if (logger.isWarnEnabled()) {
                    logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
                }
            }
        }
    }

    protected void logBeforeInterceptor(Object target, Object[] args) {
        logger.beforeInterceptor(target, args);
    }

    protected void prepareBeforeTrace(Object target, Object[] args) {

    }
    protected abstract  void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args, boolean newTrace);

    protected abstract void doInBeforeTrace(final SpanEventRecorder recorder, final Object target, final Object[] args,boolean newTrace);

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logAfterInterceptor(target, args, result, throwable);
        }

        prepareAfterTrace(target, args, result, throwable);

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if(!trace.isRootStack()) {
            try {
                final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                doInAfterTrace(recorder, target, args, result, throwable, true);
            } catch (Throwable th) {
                if (logger.isWarnEnabled()) {
                    logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
                }
            } finally {
                trace.traceBlockEnd();
            }
        }
        else{
            // TODO STATDISABLE this logic was added to disable statistics tracing,只有在本拦截器中创建的trace才需要remove
            if (!trace.canSampled()) {
                traceContext.removeTraceObject();
                return;
            }
            try {

                doInAfterTrace(trace.getSpanRecorder(), target, args, result, throwable);

            } catch (Throwable th) {
                if (logger.isWarnEnabled()) {
                    logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
                }
            } finally {


                //只有在本拦截器中创建的trace才需要remove
                traceContext.removeTraceObject();
                deleteTrace(trace, target, args, result, throwable);
            }

        }
    }

    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, args, result, throwable);
    }

    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
    }

    protected abstract void doInAfterTrace(final SpanEventRecorder recorder, final Object target, final Object[] args, final Object result, Throwable throwable,boolean newTrace);
    protected abstract void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable);
    protected MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    protected TraceContext getTraceContext() {
        return traceContext;
    }
    protected void deleteTrace(final Trace trace, final Object target, final Object[] args, final Object result, Throwable throwable) {
        trace.close();
    }



}
