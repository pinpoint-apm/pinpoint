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
import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.transport.TTransportException;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * @author koo.taejin
 */
public final class HeaderTBaseSerializerFactory implements SerializerFactory<HeaderTBaseSerializer> {

    public static final int DEFAULT_STREAM_SIZE = 1024 * 16;

    public static final TypeLocator<TBase<?, ?>> DEFAULT_TBASE_LOCATOR = DefaultTBaseLocator.getTypeLocator();

    public static final HeaderTBaseSerializerFactory DEFAULT_FACTORY = new HeaderTBaseSerializerFactory();

    private final int outputStreamSize;
    private final TypeLocator<TBase<?, ?>> locator;

    public HeaderTBaseSerializerFactory() {
        this(HeaderTBaseSerializerFactory.DEFAULT_STREAM_SIZE, DEFAULT_TBASE_LOCATOR);
    }

    public HeaderTBaseSerializerFactory(int outputStreamSize) {
        this(outputStreamSize, DEFAULT_TBASE_LOCATOR);
    }

    public HeaderTBaseSerializerFactory(TypeLocator<TBase<?, ?>> locator) {
        this(HeaderTBaseSerializerFactory.DEFAULT_STREAM_SIZE, locator);
    }

    public HeaderTBaseSerializerFactory(int outputStreamSize,
                                        TypeLocator<TBase<?, ?>> locator) {
        this.outputStreamSize = outputStreamSize;
        this.locator = Objects.requireNonNull(locator, "locator");
    }

    @Override
    public HeaderTBaseSerializer createSerializer() {
        try {
            ByteArrayOutputStream baos = new TByteArrayOutputStream(this.outputStreamSize);
            // TODO long stringLengthLimit, long containerLengthLimit
            return new HeaderTBaseSerializer(baos, ProtocolFactory.getFactory(), locator);
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean isSupport(Object target) {
        if (target instanceof TBase<?, ?>) {
            return locator.isSupport((Class<? extends TBase<?, ?>>) target.getClass());
        }

        return false;
    }

}
