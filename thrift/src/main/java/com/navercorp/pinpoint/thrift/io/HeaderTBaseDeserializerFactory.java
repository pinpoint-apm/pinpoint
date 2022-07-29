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
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.Objects;

/**
 * @author koo.taejin
 */
public final class HeaderTBaseDeserializerFactory implements DeserializerFactory<HeaderTBaseDeserializer> {

    private static final TypeLocator<TBase<?, ?>> DEFAULT_TBASE_LOCATOR = DefaultTBaseLocator.getTypeLocator();

    public static final HeaderTBaseDeserializerFactory DEFAULT_FACTORY = new HeaderTBaseDeserializerFactory();

    private final TProtocolFactory protocolFactory = ProtocolFactory.getFactory();
    private final TypeLocator<TBase<?, ?>> locator;

    public HeaderTBaseDeserializerFactory() {
        this(DEFAULT_TBASE_LOCATOR);
    }

    public HeaderTBaseDeserializerFactory(TypeLocator<TBase<?, ?>> locator) {
        this.locator = Objects.requireNonNull(locator, "locator");
    }


    @Override
    public HeaderTBaseDeserializer createDeserializer() {
        return new HeaderTBaseDeserializer(protocolFactory, locator);
    }

}
