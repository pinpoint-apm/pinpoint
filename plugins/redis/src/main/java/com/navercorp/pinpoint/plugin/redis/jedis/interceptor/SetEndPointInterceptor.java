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

import java.net.URI;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.redis.jedis.EndPointUtils;
import redis.clients.jedis.JedisShardInfo;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.redis.jedis.EndPointAccessor;

/**
 * Jedis (redis client) constructor interceptor
 * - trace endPoint
 *
 * @author jaehong.kim
 */
public class SetEndPointInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public SetEndPointInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            if (!validate(target, args)) {
                return;
            }

            final String endPoint = getEndPoint(args);
            ((EndPointAccessor) target)._$PINPOINT$_setEndPoint(endPoint);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
            }
        }
    }

    private String getEndPoint(Object[] args) {
        // first arg is host
        final Object argZero = args[0];
        if (argZero instanceof String) {
            return EndPointUtils.getEndPoint(args);
        } else if (argZero instanceof URI) {
            final URI uri = (URI) argZero;
            return HostAndPort.toHostAndPortString(uri.getHost(), uri.getPort());
        } else if (argZero instanceof JedisShardInfo) {
            final JedisShardInfo info = (JedisShardInfo) argZero;
            return HostAndPort.toHostAndPortString(info.getHost(), info.getPort());
        }
        return "Unknown";
    }

    private boolean validate(final Object target, final Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            if (isDebug) {
                logger.debug("Invalid arguments. Null or not found args({}).", args);
            }
            return false;
        }

        if (!(target instanceof EndPointAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", EndPointAccessor.class.getName());
            }
            return false;
        }
        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}