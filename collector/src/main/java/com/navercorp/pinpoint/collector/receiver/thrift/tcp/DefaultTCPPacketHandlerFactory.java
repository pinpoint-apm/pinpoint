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

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTCPPacketHandlerFactory implements TCPPacketHandlerFactory {

    private static final int DEFAULT_UDP_STREAM_MAX_SIZE = HeaderTBaseSerializerFactory.DEFAULT_UDP_STREAM_MAX_SIZE;

    private SerializerFactory<HeaderTBaseSerializer> serializerFactory;
    private DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;


    public DefaultTCPPacketHandlerFactory() {
    }


    public void setSerializerFactory(SerializerFactory<HeaderTBaseSerializer> serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public void setDeserializerFactory(DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory) {
        this.deserializerFactory = deserializerFactory;
    }

    private DeserializerFactory<HeaderTBaseDeserializer> defaultDeserializerFactory() {
        final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new HeaderTBaseDeserializerFactory();
        return new ThreadLocalHeaderTBaseDeserializerFactory<>(deserializerFactory);
    }

    private SerializerFactory<HeaderTBaseSerializer> defaultSerializerFactory() {
        final SerializerFactory<HeaderTBaseSerializer> serializerFactory = new HeaderTBaseSerializerFactory(true, DEFAULT_UDP_STREAM_MAX_SIZE);
        return new ThreadLocalHeaderTBaseSerializerFactory<>(serializerFactory);
    }


    @Override
    public TCPPacketHandler build(DispatchHandler dispatchHandler) {

        Objects.requireNonNull(dispatchHandler, "dispatchHandler");

        SerializerFactory<HeaderTBaseSerializer> serializerFactory = this.serializerFactory;
        if (serializerFactory == null) {
            serializerFactory = defaultSerializerFactory();
        }
        DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = this.deserializerFactory;
        if (deserializerFactory == null) {
            deserializerFactory = defaultDeserializerFactory();
        }
        return new DefaultTCPPacketHandler(dispatchHandler, serializerFactory, deserializerFactory);
    }





}
