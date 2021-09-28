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

package com.navercorp.pinpoint.flink.receiver;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.DefaultTCPPacketHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandlerFactory;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.FlinkHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;
import org.apache.thrift.TBase;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class FlinkPacketHandlerFactory implements TCPPacketHandlerFactory<TBase<?, ?>, TBase<?, ?>> {

    private final SerializerFactory<HeaderTBaseSerializer> cachedSerializer;
    private final DeserializerFactory<HeaderTBaseDeserializer> cachedDeserializer;

    public FlinkPacketHandlerFactory(HeaderTBaseSerializerFactory flinkHeaderTBaseSerializerFactory, FlinkHeaderTBaseDeserializerFactory flinkHeaderTBaseDeserializerFactory) {
        Objects.requireNonNull(flinkHeaderTBaseSerializerFactory, "flinkHeaderTBaseSerializerFactory");
        Objects.requireNonNull(flinkHeaderTBaseDeserializerFactory, "flinkHeaderTBaseDeserializerFactory");

        this.cachedSerializer = new ThreadLocalHeaderTBaseSerializerFactory<>(flinkHeaderTBaseSerializerFactory);

        this.cachedDeserializer = new ThreadLocalHeaderTBaseDeserializerFactory<>(flinkHeaderTBaseDeserializerFactory);
    }

    @Override
    public TCPPacketHandler build(DispatchHandler<TBase<?, ?>, TBase<?, ?>> dispatchHandler) {
        Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        return new DefaultTCPPacketHandler(dispatchHandler, cachedSerializer, cachedDeserializer);
    }
}