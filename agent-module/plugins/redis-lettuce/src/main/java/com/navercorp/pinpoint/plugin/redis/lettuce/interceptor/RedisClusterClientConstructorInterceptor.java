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

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.redis.lettuce.EndPointAccessor;
import io.lettuce.core.RedisURI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author messi-gao
 */
public class RedisClusterClientConstructorInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public RedisClusterClientConstructorInterceptor() {
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
        if (ArrayUtils.getLength(args) < 2 || args[1] == null) {
            return false;
        }

        if (!(target instanceof EndPointAccessor)) {
            return false;
        }

        if (!(args[1] instanceof Iterable)) {
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