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

package com.navercorp.pinpoint.plugin.redis.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.redis.CommandContext;
import com.navercorp.pinpoint.plugin.redis.CommandContextFactory;
import com.navercorp.pinpoint.plugin.redis.EndPointAccessor;
import com.navercorp.pinpoint.plugin.redis.RedisConstants;

/**
 * Jedis Pipeline(redis client) method interceptor
 * 
 * @author jaehong.kim
 *
 */
public class JedisPipelineMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private InterceptorScope interceptorScope;
    private boolean io;
    
    public JedisPipelineMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope, boolean io) {
        super(traceContext, methodDescriptor);
        
        this.interceptorScope = interceptorScope;
        this.io = io;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        if (invocation != null) {
            invocation.getOrCreateAttachment(CommandContextFactory.COMMAND_CONTEXT_FACTORY);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        String endPoint = null;
        if (target instanceof EndPointAccessor) {
            endPoint = ((EndPointAccessor)target)._$PINPOINT$_getEndPoint();
        }

        final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        if (invocation != null && invocation.getAttachment() != null && invocation.getAttachment() instanceof CommandContext) {
            final CommandContext commandContext = (CommandContext) invocation.getAttachment();
            logger.debug("Check command context {}", commandContext);
            if (io) {
                final StringBuilder sb = new StringBuilder();
                sb.append("write=").append(commandContext.getWriteElapsedTime());
                if (commandContext.isWriteFail()) {
                    sb.append("(fail)");
                }
                sb.append(", read=").append(commandContext.getReadElapsedTime());
                if (commandContext.isReadFail()) {
                    sb.append("(fail)");
                }
                recorder.recordAttribute(AnnotationKey.ARGS0, sb.toString());
            }
            // clear
            invocation.removeAttachment();
        }
        
        recorder.recordApi(getMethodDescriptor());
        recorder.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        recorder.recordDestinationId(RedisConstants.REDIS.getName());
        recorder.recordServiceType(RedisConstants.REDIS);
        recorder.recordException(throwable);
    }
}