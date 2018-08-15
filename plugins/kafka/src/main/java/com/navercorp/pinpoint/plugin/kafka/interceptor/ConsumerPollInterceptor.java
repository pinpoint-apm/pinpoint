/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Iterator;

/**
 * @author Taejin Koo
 */
public class ConsumerPollInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public ConsumerPollInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

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

        if (!(target instanceof RemoteAddressFieldAccessor)) {
            return;
        }

        String remoteAddress = ((RemoteAddressFieldAccessor) target)._$PINPOINT$_getRemoteAddress();
        if (StringUtils.isEmpty(remoteAddress)) {
            remoteAddress = KafkaConstants.UNKNOWN;
        }

        if (result instanceof ConsumerRecords) {
            Iterator consumerRecordIterator = ((ConsumerRecords) result).iterator();
            while (consumerRecordIterator.hasNext()) {
                Object consumerRecord = consumerRecordIterator.next();
                if (consumerRecord instanceof RemoteAddressFieldAccessor) {
                    ((RemoteAddressFieldAccessor) consumerRecord)._$PINPOINT$_setRemoteAddress(remoteAddress);
                }
            }
        }
    }

}
