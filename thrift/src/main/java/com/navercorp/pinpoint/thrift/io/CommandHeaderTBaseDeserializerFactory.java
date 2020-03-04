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
public final class CommandHeaderTBaseDeserializerFactory implements DeserializerFactory<HeaderTBaseDeserializer> {

    private static final CommandHeaderTBaseDeserializerFactory DEFAULT_INSTANCE = new CommandHeaderTBaseDeserializerFactory();

    public static CommandHeaderTBaseDeserializerFactory getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    private final DeserializerFactory<HeaderTBaseDeserializer> factory;

    public CommandHeaderTBaseDeserializerFactory() {
        TypeLocator<TBase<?, ?>> commandTbaseLocator = TCommandRegistry.build(Arrays.asList(TCommandType.values()));

        TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
        HeaderTBaseDeserializerFactory deserializerFactory = new HeaderTBaseDeserializerFactory(protocolFactory, commandTbaseLocator);

        this.factory = new ThreadLocalHeaderTBaseDeserializerFactory<HeaderTBaseDeserializer>(deserializerFactory);
    }

    @Override
    public HeaderTBaseDeserializer createDeserializer() {
        return this.factory.createDeserializer();
    }

}
