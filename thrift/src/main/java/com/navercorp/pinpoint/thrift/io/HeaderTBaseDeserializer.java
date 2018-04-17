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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.thrift.dto.ThriftRequest;
import com.navercorp.pinpoint.thrift.io.header.InvalidHeaderException;
import com.navercorp.pinpoint.thrift.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.thrift.io.header.v2.HeaderV2;
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

    private static final String KEY_VALUE_DELIMITER = ":";
    private static final String KEY_VALUE_PAIR_DELIMITER = "\r\n";

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

    public ThriftRequest deserializeThrfitRequest(byte[] bytes) throws TException {
        try {
            trans.reset(bytes);
            Header header = readHeader();
            final int validate = validate(header);
            if (validate == HeaderUtils.OK) {
                TBase<?, ?> base = locator.tBaseLookup(header.getType());
                base.read(protocol);
                return new ThriftRequest(header, base);
            }
            throw new IllegalStateException("invalid validate " + validate);
        } finally {
            trans.clear();
            protocol.reset();
        }
    }

    private Header readHeader() {
        try {
            final byte signature = protocol.readByte();
            final byte version = protocol.readByte();
            final byte type1 = protocol.readByte();
            final byte type2 = protocol.readByte();
            final short type = bytesToShort(type1, type2);

            if (signature != Header.SIGNATURE) {
                throw new IllegalArgumentException(String.format("unsupported Header : signature(0x%02X), version(0x%02X), type(%d) ", signature, version, type));
            }

            if (version == HeaderV1.VERSION) {
                return createHeaderv1(type);
            } else if (version == HeaderV2.VERSION) {
                return createHeaderV2(type, protocol);
            }

            throw new IllegalArgumentException(String.format("unsupported Header : signature(0x%02X), version(0x%02X), type(%d) ", signature, version, type));
        } catch (TException e) {
            throw new InvalidHeaderException("header is invalid.", e);
        }
    }

    private HeaderV2 createHeaderV2(short type, TProtocol protocol) throws TException {
        String dataString = protocol.readString();

        if (dataString.length() <= 2) {
            if (KEY_VALUE_PAIR_DELIMITER.equals(dataString)) {
                return new HeaderV2(type, new HashMap<String, String>(0));
            } else {
                throw new InvalidHeaderException("header have invalid header data : " + dataString);
            }
        }

        Map<String, String> data = stringToMap(dataString);

        return new HeaderV2(type, data);
    }

    private HeaderV1 createHeaderv1(short type) {
        return new HeaderV1(type);
    }

    private short bytesToShort(final byte byte1, final byte byte2) {
        return (short) (((byte1 & 0xff) << 8) | ((byte2 & 0xff)));
    }

    private Map<String, String> stringToMap(String data) {
        Map<String, String> headerData = new HashMap<String, String>();

        String[] splitData = data.split(KEY_VALUE_PAIR_DELIMITER);
        for (String entry : splitData) {
            String[] keyValue = entry.split(KEY_VALUE_DELIMITER);

            if (keyValue.length != 2) {
                throw new InvalidHeaderException("invalid data of header : " + data);
            }

            headerData.put(keyValue[0], keyValue[1]);
        }

        return headerData;
    }
}
