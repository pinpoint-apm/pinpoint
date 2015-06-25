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

import redis.clients.jedis.Client;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.redis.RedisConstants;

/**
 * 
 * @author jaehong.kim
 *
 */
public abstract class JedisClientMetadataAttchInterceptor implements SimpleAroundInterceptor, RedisConstants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MetadataAccessor endPointAccessor;

    public JedisClientMetadataAttchInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, @Name(METADATA_END_POINT) MetadataAccessor endPointAccessor) {
        this.endPointAccessor = endPointAccessor;
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

            final String endPoint = endPointAccessor.get(args[0]);
            if (endPoint != null) {
                endPointAccessor.set(target, endPoint);
            }
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            logger.debug("Invalid arguments. Null or not found args({}).", args);
            return false;
        }

        if (!(args[0] instanceof Client)) {
            logger.debug("Invalid arguments. Expectations Client but args[0]({}).", args[0]);
            return false;
        }

        if (!endPointAccessor.isApplicable(target)) {
            logger.debug("Invalid target object. Need metadata accessor({}).", METADATA_END_POINT);
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}