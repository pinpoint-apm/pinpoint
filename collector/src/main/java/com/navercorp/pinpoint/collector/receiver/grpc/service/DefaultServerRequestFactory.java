/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.common.server.io.DefaultServerRequest;
import com.navercorp.pinpoint.common.server.io.MessageType;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.io.ServerRequest;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.io.request.GrpcHeaderFactory;
import com.navercorp.pinpoint.io.request.UidFetcher;
import com.navercorp.pinpoint.io.request.UidFetchers;
import io.grpc.Context;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultServerRequestFactory implements ServerRequestFactory {
    private final GrpcHeaderFactory headerFactory;

    public DefaultServerRequestFactory() {
        this(new GrpcHeaderFactory());
    }

    public DefaultServerRequestFactory(GrpcHeaderFactory headerFactory) {
        this.headerFactory = Objects.requireNonNull(headerFactory, "headerFactory");
    }



    @Override
    public <T> ServerRequest<T> newServerRequest(Context context, MessageType messageType, T data) {
        return newServerRequest(context, UidFetchers.defaultUidFetcher(), messageType, data);
    }

    @Override
    public <T> ServerRequest<T> newServerRequest(Context context, UidFetcher uidFetcher, MessageType messageType, T data) {
        final Header header = ServerContext.getAgentInfo(context);
        final TransportMetadata transportMetadata = ServerContext.getTransportMetadata(context);
        if (transportMetadata == null) {
            throw new IllegalStateException("transportMetadata is null");
        }
        long requestTime = System.currentTimeMillis();
        ServerHeader serverHeader = headerFactory.serverHeader(header, new Supplier<ServiceUid>() {
            @Override
            public ServiceUid get() {
//                CompletableFuture<ServiceUid> future = uidFetcher.getServiceUid(ServiceUid.DEFAULT_SERVICE_UID_NAME);
                return ServiceUid.DEFAULT;
            }
        });

        return new DefaultServerRequest<>(serverHeader, transportMetadata, requestTime, messageType, data);
    }

}
