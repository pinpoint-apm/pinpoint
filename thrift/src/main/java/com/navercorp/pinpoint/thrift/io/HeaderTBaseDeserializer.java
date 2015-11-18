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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * copy->TBaseDeserializer
 */
public class HeaderTBaseDeserializer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TProtocol protocol;
    private final TMemoryInputTransport trans;
    private final TBaseLocator locator;

    /**
     * Create a new TDeserializer. It will use the TProtocol specified by the
     * factory that is passed in.
     *
     * @param protocolFactory Factory to create a protocol
     */
    HeaderTBaseDeserializer(TProtocolFactory protocolFactory, TBaseLocator locator) {
        this.trans = new TMemoryInputTransport();
        this.protocol = protocolFactory.getProtocol(trans);
        this.locator = locator;
    }

    /**
     * Deserialize the Thrift object from a byte array.
     *
     * @param bytes   The array to read from
     */
    public TBase<?, ?> deserialize(byte[] bytes) throws TException {
        try {
            trans.reset(bytes);
            Header header = readHeader();
            final int validate = validate(header);
            if (validate == HeaderUtils.OK) {
                TBase<?, ?> base = locator.tBaseLookup(header.getType());
                base.read(protocol);
                return base;
            }
            throw new IllegalStateException("invalid validate " + validate);
        } finally {
            trans.clear();
            protocol.reset();
        }
    }
    
    public List<TBase<?, ?>> deserializeList(byte[] buffer) throws TException {
        List<TBase<?, ?>> tBaseList = new ArrayList<TBase<?,?>>();
        
        trans.reset(buffer);
        try {
            while (trans.getBytesRemainingInBuffer() > 0) {
                Header header = readHeader();
                final int validate = validate(header);
                if (validate == HeaderUtils.OK) {
                    TBase<?, ?> base = locator.tBaseLookup(header.getType());
                    base.read(protocol);
                    tBaseList.add(base);
                } else {
                    throw new IllegalStateException("invalid validate " + validate);
                }
            }
        } catch (Exception e){
            logger.warn("failed to deserialize.", e);
            return new ArrayList<TBase<?,?>>();
        } finally {
            trans.clear();
            protocol.reset();
        }
        
        return tBaseList;
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
        final byte signature = protocol.readByte();
        final byte version = protocol.readByte();
        
        // fixed size regardless protocol
        final byte type1 = protocol.readByte();
        final byte type2 = protocol.readByte();
        final short type = bytesToShort(type1, type2);
        return new Header(signature, version, type);
    }

    private short bytesToShort(final byte byte1, final byte byte2) {
        return (short) (((byte1 & 0xff) << 8) | ((byte2 & 0xff)));
    }

}
