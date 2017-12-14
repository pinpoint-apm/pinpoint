/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.thrift.io;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * @author Taejin Koo
 */
public class HeaderTBaseSerializerFactory2 implements SerializerFactory<HeaderTBaseSerializer2> {

    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();
    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();

    private final TProtocolFactory protocolFactory;
    private final TBaseLocator tBaseLocator;

    public HeaderTBaseSerializerFactory2() {
        this(DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
    }

    public HeaderTBaseSerializerFactory2(TProtocolFactory protocolFactory) {
        this(protocolFactory, DEFAULT_TBASE_LOCATOR);
    }

    public HeaderTBaseSerializerFactory2(TBaseLocator tBaseLocator) {
        this(DEFAULT_PROTOCOL_FACTORY, tBaseLocator);
    }

    public HeaderTBaseSerializerFactory2(TProtocolFactory protocolFactory, TBaseLocator tBaseLocator) {
        this.protocolFactory = protocolFactory;
        this.tBaseLocator = tBaseLocator;
    }

    @Override
    public HeaderTBaseSerializer2 createSerializer() {
        return new HeaderTBaseSerializer2(protocolFactory, tBaseLocator);
    }

    @Override
    public boolean isSupport(Object target) {
        if (target instanceof TBase) {
            return tBaseLocator.isSupport((Class<? extends TBase>) target.getClass());
        }

        return false;
    }

}
