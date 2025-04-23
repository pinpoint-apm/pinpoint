/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka.util;

import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.EndPointFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KafkaRequestAdaptor implements RequestAdaptor<ConsumerRecord> {

    public KafkaRequestAdaptor() {
    }

    @Override
    public String getHeader(ConsumerRecord request, String name) {
        try {
            for (Header header : request.headers()) {
                if (header.key().equals(name)) {
                    return BytesUtils.toString(header.value());
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    @Override
    public Collection<String> getHeaderNames(ConsumerRecord request) {
        List<String> headerNames = new ArrayList<>();
        try {
            for (Header header : request.headers()) {
                headerNames.add(header.key());
            }
        } catch (Exception ignored) {
        }

        return headerNames;
    }

    @Override
    public String getRpcName(ConsumerRecord request) {
        StringBuilder rpcName = new StringBuilder("kafka://");
        try {
            rpcName.append("topic=").append(request.topic());
            rpcName.append("?partition=").append(request.partition());
            rpcName.append("&offset=").append(request.offset());
        } catch (Exception ignored) {
        }

        return rpcName.toString();
    }

    @Override
    public String getMethodName(ConsumerRecord request) {
        return null;
    }

    @Override
    public String getEndPoint(ConsumerRecord request) {
        if (request instanceof EndPointFieldAccessor) {
            return ((EndPointFieldAccessor) request)._$PINPOINT$_getEndPoint();
        }

        return null;
    }

    @Override
    public String getRemoteAddress(ConsumerRecord request) {
        if (request instanceof RemoteAddressFieldAccessor) {
            return ((RemoteAddressFieldAccessor) request)._$PINPOINT$_getRemoteAddress();
        }

        return null;
    }

    @Override
    public String getAcceptorHost(ConsumerRecord request) {
        return getRemoteAddress(request);
    }
}
