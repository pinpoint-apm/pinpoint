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

package com.navercorp.pinpoint.plugin.jackson;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class JacksonUtils {

    public static boolean addInterceptor(InstrumentMethod method, Class<? extends Interceptor> interceptorClassName) {
        if (method != null) {
            try {
                method.addScopedInterceptor(interceptorClassName, JacksonConstants.JACKSON_SCOPE);
                return true;
            } catch (InstrumentException e) {
                final PLogger logger = PLoggerFactory.getLogger(JacksonUtils.class);
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + method, e);
                }
            }
        }
        return false;
    }

    public static boolean addInterceptor(InstrumentMethod method, Class<? extends Interceptor> interceptorClassName, Object[] constructorArgs) {
        if (method != null) {
            try {
                method.addScopedInterceptor(interceptorClassName, constructorArgs, JacksonConstants.JACKSON_SCOPE);
                return true;
            } catch (InstrumentException e) {
                final PLogger logger = PLoggerFactory.getLogger(JacksonUtils.class);
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + method, e);
                }
            }
        }
        return false;
    }
}
