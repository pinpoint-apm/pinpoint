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
import java.util.List;

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
        List<TBase<?, ?>> list = new ArrayList<TBase<?, ?>>();
        try {
            trans.reset(bytes, offset, length);

            final Header header = readHeader();
            if(header == null) {
                return list;
            }
            
            if (locator.isChunkHeader(header.getType())) {
                TBase<?, ?> base;
                while ((base = deserialize()) != null) {
                    list.add(base);
                }
            } else {
                TBase<?, ?> base = deserialize();
                if (base != null) {
                    list.add(base);
                }
            }

        } finally {
            trans.clear();
            protocol.reset();
        }

        return list;
    }

    private TBase<?, ?> deserialize() throws TException {
        final Header header = readHeader();
        if (header == null) {
            return null;
        }

        final int validate = validate(header);

        TBase<?, ?> base = locator.tBaseLookup(header.getType());
        base.read(protocol);
        return base;
    }

    private int validate(Header header) throws TException {
        final byte signature = header.getSignature();
        final int result = HeaderUtils.validateSignature(signature);
        if (result == HeaderUtils.FAIL) {
            throw new TException("Invalid Signature:" + header);
        }
        return result;
    }

    private Header readHeader() throws TException {
        if (trans.getBytesRemainingInBuffer() < Header.HEADER_SIZE) {
            return null;
        }

        final byte signature = protocol.readByte();
        final byte version = protocol.readByte();
        final byte type1 = protocol.readByte();
        final byte type2 = protocol.readByte();
        final short type = bytesToShort(type1, type2);
        return new Header(signature, version, type);
    }

    private short bytesToShort(final byte byte1, final byte byte2) {
        return (short) (((byte1 & 0xff) << 8) | ((byte2 & 0xff)));
    }
}