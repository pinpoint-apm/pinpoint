/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.EndPointFieldAccessor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class ConsumerRecordsInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (args == null || args.length != 1) {
            return;
        }
        if (!(args[0] instanceof Map)) {
            return;
        }

        Map consumerRecordsMap = (Map) args[0];
        Set<Map.Entry> entrySet = consumerRecordsMap.entrySet();
        for (Map.Entry entry : entrySet) {
            if (entry == null) {
                continue;
            }

            final String endPoint = getEndPoint(entry.getKey());
            if (StringUtils.hasText(endPoint)) {
                Object value = entry.getValue();
                if (value instanceof List) {
                    List consumerRecordList = (List) value;
                    for (Object endPointFieldAccessor : consumerRecordList) {
                        if (endPointFieldAccessor instanceof EndPointFieldAccessor) {
                            ((EndPointFieldAccessor) endPointFieldAccessor)._$PINPOINT$_setEndPoint(endPoint);
                        }
                    }
                }
            }
        }
    }

    private String getEndPoint(Object endPointFieldAccessor) {
        if (endPointFieldAccessor instanceof EndPointFieldAccessor) {
            return ((EndPointFieldAccessor) endPointFieldAccessor)._$PINPOINT$_getEndPoint();
        }

        return null;
    }

}
