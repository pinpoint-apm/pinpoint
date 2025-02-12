/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.io.util.MessageType;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultServerRequest<T> extends DefaultAttributeMap implements ServerRequest<T> {
    private final Header header;
    private final TransportMetadata transport;
    private final long requestTime;
    private final MessageType messageType;
    private final T data;

    public DefaultServerRequest(Header header, TransportMetadata transport, long requestTime, MessageType messageType, T data) {
        this.header = Objects.requireNonNull(header, "header");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.requestTime = requestTime;
        this.messageType = Objects.requireNonNull(messageType, "messageType");
        this.data = data;
    }

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public long getRequestTime() {
        return requestTime;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public String getRemoteAddress() {
        return transport.getRemoteAddress().getHostString();
    }

    @Override
    public int getRemotePort() {
        return transport.getRemoteAddress().getPort();
    }

    public Long getTransportId() {
        return transport.getTransportId();
    }

    @Override
    public String toString() {
        return "DefaultServerRequest{" +
                "header=" + header +
                ", messageType=" + messageType +
                ", data=" + data +
                ", address='" + transport.getRemoteAddress() + '\'' +
                '}';
    }
}
