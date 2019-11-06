/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.redis.redisson.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.redis.redisson.RedissonConstants;
import com.navercorp.pinpoint.plugin.redis.redisson.RedissonPluginConfig;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.connection.NodeSource;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * @author jaehong.kim
 */
public class CommandAsyncServiceMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final boolean keyTrace;

    public CommandAsyncServiceMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);

        final RedissonPluginConfig config = new RedissonPluginConfig(traceContext.getProfilerConfig());
        this.keyTrace = config.isKeyTrace();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        try {
            if (isAsynchronousInvocation(target, args)) {
                // set asynchronous trace
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                // type check isAsynchronousInvocation
                ((AsyncContextAccessor) args[5])._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set AsyncContext {}", asyncContext);
                }
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    private boolean isAsynchronousInvocation(final Object target, final Object[] args) {
        if (args == null || args.length < 6) {
            return false;
        }

        if (!(args[5] instanceof AsyncContextAccessor)) {
            if (isDebug) {
                logger.debug("Invalid result object. Need accessor({}).", AsyncContextAccessor.class.getName());
            }
            return false;
        }

        return true;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (validate(target, args)) {
            final String endPoint = toEndPoint((NodeSource) args[1]);
            recorder.recordEndPoint(endPoint);
            if (isDebug) {
                logger.debug("Record endpoint {}", endPoint);
            }

            if (this.keyTrace) {
                RedisCommand redisCommands = (RedisCommand) args[3];
                if (redisCommands != null && StringUtils.hasLength(redisCommands.getName())) {
                    recorder.recordAttribute(AnnotationKey.ARGS0, redisCommands.getName());
                }
            }
        } else {
            recorder.recordEndPoint("Unknown");
        }

        recorder.recordApi(this.methodDescriptor);
        recorder.recordDestinationId(RedissonConstants.REDISSON.getName());
        recorder.recordServiceType(RedissonConstants.REDISSON);
        recorder.recordException(throwable);
    }

    private boolean validate(final Object target, final Object[] args) {
        if (args == null || args.length < 4) {
            if (isDebug) {
                logger.debug("Invalid arguments. Null or not found args({}).", args);
            }
            return false;
        }
        // args[1] is org.redisson.connection.NodeSource
        if (!(args[1] instanceof NodeSource)) {
            if (isDebug) {
                logger.debug("Invalid arguments. Expect NodeSourceGetter but args[0]({}).", args[0]);
            }
            return false;
        }
        // args[3] is org.redisson.client.protocol.RedisCommand
        if (!(args[3] instanceof RedisCommand)) {
            if (isDebug) {
                logger.debug("Invalid arguments. Expect RedisCommand but args[3]({}).", args[0]);
            }
            return false;
        }

        return true;
    }

    private String toEndPoint(NodeSource nodeSource) {
        try {
            // TODO
            if (nodeSource.getRedirect() != null) {
                final URI uri = nodeSource.getAddr();
                if (uri != null) {
                    return HostAndPort.toHostAndPortString(uri.getHost(), uri.getPort());
                }
            } else if (nodeSource.getRedisClient() != null) {
                final InetSocketAddress address = nodeSource.getRedisClient().getAddr();
                return HostAndPort.toHostAndPortString(address.getHostString(), address.getPort());
            } else {
                final MasterSlaveEntry entry = nodeSource.getEntry();
                if (entry != null) {
                    if (entry.getClient() != null) {
                        final InetSocketAddress address = entry.getClient().getAddr();
                        return HostAndPort.toHostAndPortString(address.getHostString(), address.getPort());
                    }
                } else if (nodeSource.getSlot() != null) {
                    return "slot=" + nodeSource.getSlot();
                }

            }
        } catch (Exception ignored) {
            if (isDebug) {
                logger.debug("Unexpected error. nodeSource={}", nodeSource, ignored);
            }
        }
        return "Unknown";
    }
}