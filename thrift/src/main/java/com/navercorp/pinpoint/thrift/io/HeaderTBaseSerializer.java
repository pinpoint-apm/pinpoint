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


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.navercorp.pinpoint.thrift.io.header.InvalidHeaderException;
import com.navercorp.pinpoint.thrift.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.thrift.io.header.v2.HeaderV2;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;

/**
 * Generic utility for easily serializing objects into a byte array or Java
 * String.
 * copy->HeaderTBaseSerializer
 */
public class HeaderTBaseSerializer {

    public static final int HEADER_DATA_MAX_SIZE = 64;
    public static final int HEADER_DATA_STRING_MAX_LANGTH = 1024;

    private final ResettableByteArrayOutputStream baos;
    private final TProtocol protocol;
    private final TBaseLocator locator;

    /**
     * Create a new HeaderTBaseSerializer.
     */
    HeaderTBaseSerializer(ResettableByteArrayOutputStream bos, TProtocolFactory protocolFactory, TBaseLocator locator) {
        this.baos = bos;
        TIOStreamTransport transport = new TIOStreamTransport(bos);
        this.protocol = protocolFactory.getProtocol(transport);
        this.locator = locator;
    }

    /**
     * Serialize the Thrift object into a byte array. The process is simple,
     * just clear the byte array output, write the object into it, and grab the
     * raw bytes.
     *
     * @param base The object to serialize
     * @return Serialized object in byte[] format
     */
    public byte[] serialize(TBase<?, ?> base) throws TException {
        final Header header = locator.headerLookup(base);
        baos.reset();
        writeHeader(header);
        base.write(protocol);
        return baos.toByteArray();
    }

    public byte[] continueSerialize(TBase<?, ?> base) throws TException {
        final Header header = locator.headerLookup(base);
        writeHeader(header);
        base.write(protocol);
        return baos.toByteArray();
    }

    public void reset() {
        baos.reset();
    }

    public void reset(int resetIndex) {
        baos.reset(resetIndex);
    }

    public int getInterBufferSize() {
        return baos.size();
    }

    private void writeHeader(Header header) throws TException {
        byte version = header.getVersion();

        try{
            if (version == HeaderV1.VERSION) {
                writeHeaderV1((HeaderV1)header);
            } else if (version == HeaderV2.VERSION) {
                writeHeaderV2((HeaderV2) header);
            } else {
                throw new InvalidHeaderException("can not find header version. header : " + header);
            }
        } catch (Exception e) {
            throw new InvalidHeaderException("can not write header. header : " + header, e);
        }

    }

    private void writeHeaderV2(HeaderV2 header) throws TException, UnsupportedEncodingException {
        protocol.writeByte(header.getSignature());
        protocol.writeByte(header.getVersion());

        writeShort(header.getType());

        writeHeaderData(header.getData());
    }

    private void writeHeaderData(Map<String, String> data) throws TException, UnsupportedEncodingException {
        final int size = data.size();
        if (size >= HEADER_DATA_MAX_SIZE) {
            throw new InvalidHeaderException("header size is to big. size : " + size);
        }
        writeShort((short)size);
        if (size == 0) {
            return;
        }

        for (Map.Entry<String, String> entry : data.entrySet()) {
            writeString(entry.getKey());
            writeString(entry.getValue());
        }
    }

    private void writeShort(short value) throws TException {
        protocol.writeByte(BytesUtils.readByte1ForShort(value));
        protocol.writeByte(BytesUtils.readByte2ForShort(value));
    }

    private void writeString(String value) throws TException, UnsupportedEncodingException {
        if (!validCheck(value)) {
            throw new InvalidHeaderException("string length is invalid in header data. value : " + value);
        }
        
        writeShort((short)value.length());

        byte[] valueBytes = value.getBytes(BytesUtils.UTF_8);
        for (int index = 0 ; index < valueBytes.length ; index++) {
            protocol.writeByte(valueBytes[index]);
        }
    }

    private boolean validCheck(String value) {
        int length = value.length();

        if (length > HEADER_DATA_STRING_MAX_LANGTH || length == 0) {
            return false;
        }

        return true;
    }

    private void writeHeaderV1(HeaderV1 header) throws TException {
        protocol.writeByte(header.getSignature());
        protocol.writeByte(header.getVersion());
        writeShort(header.getType());
    }

    /**
     * Serialize the Thrift object into a Java string, using the UTF8
     * charset encoding.
     *
     * @param base The object to serialize
     * @return Serialized object as a String
     */
    public String toString(TBase<?, ?> base) throws TException {
        return toString(base, BytesUtils.UTF_8_NAME);
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
