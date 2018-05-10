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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.navercorp.pinpoint.io.header.ByteArrayHeaderReader;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderReader;
import com.navercorp.pinpoint.io.header.InvalidHeaderException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TMemoryInputTransport;

/**
 * Deserialize chunked packets 
 * 
 * @author jaehong.kim
 *
 */
public class ChunkHeaderTBaseDeserializer {
    private final TProtocol protocol;
    private final TMemoryInputTransport trans;
    private final TBaseLocator locator;

    ChunkHeaderTBaseDeserializer(TProtocolFactory protocolFactory, TBaseLocator locator) {
        this.trans = new TMemoryInputTransport();
        this.protocol = protocolFactory.getProtocol(trans);
        this.locator = locator;
    }

    public List<TBase<?, ?>> deserialize(byte[] bytes, int offset, int length) throws TException {

        try {
            trans.reset(bytes, offset, length);

            Header header = readHeader();

            if (locator.isChunkHeader(header.getType())) {

                List<TBase<?, ?>> list = new ArrayList<TBase<?, ?>>();

                while (trans.getBytesRemainingInBuffer() > 0) {
                    TBase<?, ?> base = readInternal();
                    list.add(base);

                }
                return list;

            } else {
                final TBase<?, ?> base = readInternal();
                if (base == null) {
                    return Collections.emptyList();
                }
                List<TBase<?, ?>> list = new ArrayList<TBase<?, ?>>();
                list.add(base);
                return list;
            }
        } finally {
            trans.clear();
            protocol.reset();
        }

    }

    private TBase<?, ?> readInternal() throws TException {
        Header header = readHeader();

        TBase<?, ?> base = locator.tBaseLookup(header.getType());
        base.read(protocol);
        return base;
    }

    private Header readHeader() throws TException {
        HeaderReader reader = newHeaderReader();
        Header header = readHeader(reader);
        skipHeaderOffset(reader);
        return header;
    }


    private void skipHeaderOffset(HeaderReader reader) {
        trans.reset(trans.getBuffer(), reader.getOffset(), reader.getRemaining());
    }

    private ByteArrayHeaderReader newHeaderReader() {
        byte[] buffer = trans.getBuffer();
        int bufferPosition = trans.getBufferPosition();
        int bytesRemainingInBuffer = trans.getBytesRemainingInBuffer();
        return new ByteArrayHeaderReader(buffer, bufferPosition, bytesRemainingInBuffer);
    }

    private Header readHeader(HeaderReader reader) throws TException {
        try {
            return reader.readHeader();
        } catch (InvalidHeaderException e) {
            throw new TException("invalid header Caused by:" + e.getMessage(), e);
        }
    }

}