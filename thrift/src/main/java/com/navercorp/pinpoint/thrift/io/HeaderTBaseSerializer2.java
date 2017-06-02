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
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.io.OutputStream;

/**
 *
 * Caution. not thread safe
 *
 * @author Taejin Koo
 */
public class HeaderTBaseSerializer2 {

    private static final String UTF8 = "UTF8";

    private final TOutputStreamTransport tOutputStreamTransport;
    private final TProtocol protocol;
    private final TBaseLocator tBaseLocator;

    public HeaderTBaseSerializer2(TProtocolFactory protocolFactory, TBaseLocator tBaseLocator) {
        this.tOutputStreamTransport = new TOutputStreamTransport();
        this.protocol = protocolFactory.getProtocol(tOutputStreamTransport);
        this.tBaseLocator = tBaseLocator;
    }

    public void serialize(TBase<?, ?> base, OutputStream outputStream) throws TException {
        tOutputStreamTransport.open(outputStream);
        try {
            final Header header = tBaseLocator.headerLookup(base);
            writeHeader(header);
            base.write(protocol);
        } finally {
            tOutputStreamTransport.close();
        }
    }

    private void writeHeader(Header header) throws TException {
        protocol.writeByte(header.getSignature());
        protocol.writeByte(header.getVersion());
        // fixed size regardless protocol
        short type = header.getType();
        protocol.writeByte(BytesUtils.writeShort1(type));
        protocol.writeByte(BytesUtils.writeShort2(type));
    }

}
