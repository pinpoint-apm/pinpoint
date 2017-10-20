/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.thrift.io;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.Arrays;

/**
 * @author koo.taejin
 */
public final class CommandHeaderTBaseSerializerFactory implements SerializerFactory<HeaderTBaseSerializer> {

    public static final int DEFAULT_SERIALIZER_MAX_SIZE = 1024 * 64;

    private final TBaseLocator tBaseLocator;
    private final SerializerFactory<HeaderTBaseSerializer> factory;

    public CommandHeaderTBaseSerializerFactory() {
        this(DEFAULT_SERIALIZER_MAX_SIZE);
    }

    public CommandHeaderTBaseSerializerFactory(int outputStreamSize) {
        TBaseLocator commandTbaseLocator = new TCommandRegistry(Arrays.asList(TCommandType.values()));

        TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
        HeaderTBaseSerializerFactory serializerFactory = new HeaderTBaseSerializerFactory(true, outputStreamSize, protocolFactory, commandTbaseLocator);

        this.tBaseLocator = commandTbaseLocator;
        this.factory = new ThreadLocalHeaderTBaseSerializerFactory<HeaderTBaseSerializer>(serializerFactory);
    }

    @Override
    public HeaderTBaseSerializer createSerializer() {
        return this.factory.createSerializer();
    }

    @Override
    public boolean isSupport(Object target) {
        if (target instanceof TBase) {
            return tBaseLocator.isSupport((Class<? extends TBase>) target.getClass());
        }

        return false;
    }

}
