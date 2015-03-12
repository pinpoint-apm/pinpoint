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

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.Name;
import com.navercorp.pinpoint.plugin.redis.RedisConstants;

/**
 * Jedis client(redis client) constructor interceptor - trace endPoint
 * 
 * @author jaehong.kim
 *
 */
public class JedisClientConstructorInterceptor implements SimpleAroundInterceptor, RedisConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MetadataAccessor endPointAccessor;

    public JedisClientConstructorInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_END_POINT) MetadataAccessor endPointAccessor) {
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

            final StringBuilder endPoint = new StringBuilder();
            // first argument is host
            endPoint.append(args[0]);

            // second argument is port
            if (args.length >= 2 && args[1] instanceof Integer) {
                endPoint.append(":").append(args[1]);
            } else {
                // set default port
                endPoint.append(":").append(6379);
            }
            endPointAccessor.set(target, endPoint.toString());
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            logger.debug("Invalid arguments. Null or not found args({}).", args);
            return false;
        }

        if (!(args[0] instanceof String)) {
            logger.debug("Invalid arguments. Expect String but args[0]({}).", args[0]);
            return false;
        }

        if (!endPointAccessor.isApplicable(target)) {
            logger.debug("Invalid target object. Need metadata accessor({}).", METADATA_DESTINATION_ID);
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}