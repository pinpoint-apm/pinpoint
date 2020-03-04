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

package com.navercorp.pinpoint.collector.receiver.thrift.tcp;

import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


/**
 * @author Woonduk Kang(emeroad)
 */
public class TCPServerResponse implements ServerResponse<TBase<?, ?>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SerializerFactory<HeaderTBaseSerializer> serializerFactory;
    private final PinpointSocket pinpointSocket;
    private final int requestId;

    private boolean closed = false;

    public TCPServerResponse(SerializerFactory<HeaderTBaseSerializer> serializerFactory, PinpointSocket pinpointSocket, int requestId) {
        this.serializerFactory = Objects.requireNonNull(serializerFactory, "serializerFactory");
        this.pinpointSocket = Objects.requireNonNull(pinpointSocket, "pinpointSocket");
        this.requestId = requestId;
    }

    @Override
    public void write(TBase<?, ?> message) {
        if (message == null) {
            throw new NullPointerException("message");
        }
        if (closed) {
            throw new IllegalStateException("ServerResponse is closed");
        }
        closed = true;
        try {
            HeaderTBaseSerializer serializer = serializerFactory.createSerializer();
            byte[] resultBytes = serializer.serialize(message);
            pinpointSocket.response(requestId, resultBytes);
        } catch (TException e) {
            handleTException(message,  e);
        }
    }

    private void handleTException(TBase<?,?> message, TException e) {
        if (logger.isWarnEnabled()) {
            logger.warn("packet serialize error. message:{} cause:{}", message, e.getMessage(), e);
        }
        // TODO
        throw new RuntimeException("message serialize fail", e);
    }
}
