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


import com.navercorp.pinpoint.io.header.ByteArrayHeaderWriter;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.HeaderWriter;
import com.navercorp.pinpoint.io.header.InvalidHeaderException;
import com.navercorp.pinpoint.io.util.TypeLocator;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Generic utility for easily serializing objects into a byte array or Java
 * String.
 * copy->HeaderTBaseSerializer
 */
public class HeaderTBaseSerializer implements TBaseSerializer {

    private static final String UTF8 = StandardCharsets.UTF_8.name();
    private final ByteArrayOutputStream baos;

    private final TIOStreamTransport transport;
    private final TProtocol protocol;
    private final TypeLocator<TBase<?, ?>> locator;

    /**
     * Create a new HeaderTBaseSerializer. 
     */
    HeaderTBaseSerializer(ByteArrayOutputStream baos, TProtocolFactory protocolFactory, TypeLocator<TBase<?, ?>> locator) throws TTransportException {
        this.baos = Objects.requireNonNull(baos, "baos");
        // TConfiguration is no effect when message write
        this.transport = new TIOStreamTransport(ConfigurationFactory.getConfiguration(), baos);
        this.protocol = protocolFactory.getProtocol(transport);
        this.locator = Objects.requireNonNull(locator, "locator");
    }

    /**
     * Serialize the Thrift object into a byte array. The process is simple,
     * just clear the byte array output, write the object into it, and grab the
     * raw bytes.
     *
     * @param base The object to serialize
     * @return Serialized object in byte[] format
     */
    @Override
    public byte[] serialize(TBase<?, ?> base) throws TException {
        return serialize(base, HeaderEntity.EMPTY_HEADER_ENTITY);
    }

    @Override
    public byte[] serialize(TBase<?, ?> base, HeaderEntity headerEntity) throws TException {

        writeHeader(base, headerEntity);
        base.write(protocol);
        byte[] bytes = baos.toByteArray();
        baos.reset();
        return bytes;
    }

    private void writeHeader(TBase<?, ?> base, HeaderEntity headerEntity) {
        try {
            final Header header = locator.headerLookup(base);
            if (header == null) {
                throw new TException("header must not be null base:" + base);
            }
            HeaderWriter headerWriter = new ByteArrayHeaderWriter(header, headerEntity);
            byte[] headerBytes = headerWriter.writeHeader();
            baos.write(headerBytes);
        } catch (Exception e) {
            throw new InvalidHeaderException("can not write header.", e);
        }
    }

    
    public void reset() {
        baos.reset();
    }
    

    /**
     * Serialize the Thrift object into a Java string, using the UTF8
     * charset encoding.
     *
     * @param base The object to serialize
     * @return Serialized object as a String
     */
    public String toString(TBase<?, ?> base) throws TException {
        return toString(base, UTF8);
    }
 
    /**
     * Serialize the Thrift object into a Java string, using a specified
     * character set for encoding.
     *
     * @param base    The object to serialize
     * @param charset Valid JVM charset
     * @return Serialized object as a String
     */
    public String toString(TBase<?, ?> base, String charset) throws TException {
        try {
            return new String(serialize(base), charset);
        } catch (UnsupportedEncodingException uex) {
            throw new TException("JVM DOES NOT SUPPORT ENCODING: " + charset);
        }
    }

   
}
