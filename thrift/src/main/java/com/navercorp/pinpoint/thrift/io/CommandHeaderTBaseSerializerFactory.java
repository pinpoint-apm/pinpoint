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

package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.io.util.TypeLocator;
import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.Arrays;

/**
 * @author koo.taejin
 */
public final class CommandHeaderTBaseSerializerFactory implements SerializerFactory<HeaderTBaseSerializer> {

    private static final CommandHeaderTBaseSerializerFactory DEFAULT_INSTANCE = new CommandHeaderTBaseSerializerFactory();

    public static CommandHeaderTBaseSerializerFactory getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static final int DEFAULT_SERIALIZER_MAX_SIZE = 1024 * 64;

    private final SerializerFactory<HeaderTBaseSerializer> factory;

    public CommandHeaderTBaseSerializerFactory() {
        this(DEFAULT_SERIALIZER_MAX_SIZE);
    }

    public CommandHeaderTBaseSerializerFactory(int outputStreamSize) {
        TypeLocator<TBase<?, ?>> commandTbaseLocator = TCommandRegistry.build(Arrays.asList(TCommandType.values()));

        TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
        HeaderTBaseSerializerFactory serializerFactory = new HeaderTBaseSerializerFactory(true, outputStreamSize, protocolFactory, commandTbaseLocator);
        this.factory = new ThreadLocalHeaderTBaseSerializerFactory<HeaderTBaseSerializer>(serializerFactory);
    }

    @Override
    public HeaderTBaseSerializer createSerializer() {
        return this.factory.createSerializer();
    }

    @Override
    public boolean isSupport(Object target) {
        return factory.isSupport(target);
    }

}
