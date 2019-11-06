/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.redis.jedis.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.plugin.redis.jedis.CommandContext;
import com.navercorp.pinpoint.plugin.redis.jedis.CommandContextFactory;
import com.navercorp.pinpoint.plugin.redis.jedis.EndPointAccessor;
import com.navercorp.pinpoint.plugin.redis.jedis.JedisConstants;

/**
 * Jedis (redis client) method interceptor
 *
 * @author jaehong.kim
 */
public class JedisMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private InterceptorScope interceptorScope;
    private boolean io;

    public JedisMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope, boolean io) {
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
            endPoint = ((EndPointAccessor) target)._$PINPOINT$_getEndPoint();
        }

        final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        final Object attachment = getAttachment(invocation);
        if (attachment instanceof CommandContext) {
            final CommandContext commandContext = (CommandContext) attachment;
            if (logger.isDebugEnabled()) {
                logger.debug("Check command context {}", commandContext);
            }
            recordIo(recorder, commandContext);
            // clear
            invocation.removeAttachment();
        }

        recorder.recordApi(getMethodDescriptor());
        recorder.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        recorder.recordDestinationId(JedisConstants.REDIS.getName());
        recorder.recordServiceType(JedisConstants.REDIS);
        recorder.recordException(throwable);
    }

    private void recordIo(SpanEventRecorder recorder, CommandContext callContext) {
        if (io) {
            IntBooleanIntBooleanValue value = new IntBooleanIntBooleanValue((int) callContext.getWriteElapsedTime(), callContext.isWriteFail(), (int) callContext.getReadElapsedTime(), callContext.isReadFail());
            recorder.recordAttribute(AnnotationKey.REDIS_IO, value);
        }
    }

    private Object getAttachment(InterceptorScopeInvocation invocation) {
        if (invocation == null) {
            return null;
        }
        return invocation.getAttachment();
    }
}