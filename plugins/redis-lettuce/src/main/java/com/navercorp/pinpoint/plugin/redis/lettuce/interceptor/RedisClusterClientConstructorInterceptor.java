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

package com.navercorp.pinpoint.plugin.redis.lettuce.interceptor;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.redis.lettuce.EndPointAccessor;

import io.lettuce.core.RedisURI;

/**
 * @author messi-gao
 */
public class RedisClusterClientConstructorInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public RedisClusterClientConstructorInterceptor(final TraceContext traceContext,
                                                    final MethodDescriptor methodDescriptor) {
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

            final Iterable<RedisURI> redisURIs = (Iterable<RedisURI>) args[1];
            final String endPoint = getEndPoint(redisURIs);
            ((EndPointAccessor) target)._$PINPOINT$_setEndPoint(endPoint);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
            }
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (args == null || args.length < 2 || args[1] == null) {
            if (isDebug) {
                logger.debug("Invalid arguments. Null or not found args({}).", args);
            }
            return false;
        }

        if (!(target instanceof EndPointAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).",
                             EndPointAccessor.class.getName());
            }
            return false;
        }

        if (!(args[1] instanceof Iterable)) {
            if (isDebug) {
                logger.debug("Invalid args[1] object. args[1]={}", args[1]);
            }
            return false;
        }
        return true;
    }

    private String getEndPoint(Iterable<RedisURI> redisURIs) {
        final List<String> endPoints = new ArrayList<>();
        for (RedisURI redisURI : redisURIs) {
            final String hostAndPort = HostAndPort.toHostAndPortString(redisURI.getHost(), redisURI.getPort());
            endPoints.add(hostAndPort);
        }
        return endPoints.isEmpty() ? null : endPoints.toString();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}