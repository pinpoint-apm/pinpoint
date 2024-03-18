/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.redis.jedis;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.redis.jedis.interceptor.JedisMethodInterceptor;
import com.navercorp.pinpoint.plugin.redis.jedis.interceptor.SetEndPointInterceptor;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class JedisUtils {

    private JedisUtils() {
    }

    public static void addSetEndPointInterceptor(final InstrumentClass target, final String... parameterTypes) throws InstrumentException {
        final InstrumentMethod method = target.getConstructor(parameterTypes);
        if (method != null) {
            method.addInterceptor(SetEndPointInterceptor.class);
        }
    }

    public static void addJedisMethodInterceptor(final InstrumentClass target, final JedisPluginConfig config, final String scope) {
        JedisMethodNameFilter jedisMethodNameFilter = new JedisMethodNameFilter();
        for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(jedisMethodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
            try {
                method.addScopedInterceptor(JedisMethodInterceptor.class, va(config.isIo()), scope);
            } catch (Exception e) {
                final PLogger logger = PLoggerFactory.getLogger(JedisUtils.class.getClass());
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method {}", method, e);
                }
            }
        }
    }
}
