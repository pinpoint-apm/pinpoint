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

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.io.ByteArrayOutputStream;

/**
 * @author koo.taejin
 */
public final class HeaderTBaseSerializerFactory implements SerializerFactory<HeaderTBaseSerializer> {

    private static final boolean DEFAULT_SAFE_GURANTEED = true;

    public static final int DEFAULT_STREAM_SIZE = 1024 * 8;
    public static final int DEFAULT_UDP_STREAM_MAX_SIZE = 1024 * 64;
    private static final boolean DEFAULT_AUTO_EXPAND = true;

    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();
    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    public static final HeaderTBaseSerializerFactory DEFAULT_FACTORY = new HeaderTBaseSerializerFactory();

    private final boolean safetyGuranteed;
    private final int outputStreamSize;
    private final boolean autoExpand;
    private final TProtocolFactory protocolFactory;
    private final TBaseLocator locator;

    public HeaderTBaseSerializerFactory() {
        this(DEFAULT_SAFE_GURANTEED);
    }

    public HeaderTBaseSerializerFactory(boolean safetyGuranteed) {
        this(safetyGuranteed, DEFAULT_STREAM_SIZE);
    }

    public HeaderTBaseSerializerFactory(boolean safetyGuranteed, int outputStreamSize) {
        this(safetyGuranteed, outputStreamSize, DEFAULT_AUTO_EXPAND);
    }
    
    public HeaderTBaseSerializerFactory(boolean safetyGuranteed, int outputStreamSize, boolean autoExpand) {
        this(safetyGuranteed, outputStreamSize, autoExpand, DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
    }
    
    public HeaderTBaseSerializerFactory(boolean safetyGuranteed, int outputStreamSize, TProtocolFactory protocolFactory, TBaseLocator locator) {
        this(safetyGuranteed, outputStreamSize, DEFAULT_AUTO_EXPAND, DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
    }
    
    public HeaderTBaseSerializerFactory(boolean safetyGuranteed, int outputStreamSize, boolean autoExpand, TProtocolFactory protocolFactory, TBaseLocator locator) {
        this.safetyGuranteed = safetyGuranteed;
        this.outputStreamSize = outputStreamSize;
        this.autoExpand = autoExpand;
        this.protocolFactory = protocolFactory;
        this.locator = locator;
    }

    public boolean isSafetyGuranteed() {
        return safetyGuranteed;
    }

    public int getOutputStreamSize() {
        return outputStreamSize;
    }

    public TProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    public TBaseLocator getLocator() {
        return locator;
    }

    @Override
    public HeaderTBaseSerializer createSerializer() {
        ByteArrayOutputStream baos = null;
        if (safetyGuranteed) {
            baos = new PinpointByteArrayOutputStream(outputStreamSize, autoExpand);
        } else {
            baos = new UnsafeByteArrayOutputStream(outputStreamSize, autoExpand);
        }

        return new HeaderTBaseSerializer(baos, protocolFactory, locator);
    }

}
