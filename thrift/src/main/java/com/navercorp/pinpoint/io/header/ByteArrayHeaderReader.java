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

package com.navercorp.pinpoint.io.header;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ByteArrayHeaderReader implements HeaderReader {

    private final Buffer buffer;

    public ByteArrayHeaderReader(byte[] bytes) {

        this(checkBytes(bytes),0, bytes.length);
    }

    public ByteArrayHeaderReader(byte[] bytes, final int startOffset, final int length) {
        checkBytes(bytes);
        this.buffer = new OffsetFixedBuffer(bytes, startOffset, length);
    }

    private static byte[] checkBytes(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        return bytes;
    }


    public Header readHeader() {
        final byte signature = buffer.readByte();
        if (signature != Header.SIGNATURE) {
            throw new InvalidHeaderException("invalid signature :"  + signature);
        }

        final byte version = buffer.readByte();
        final short type = buffer.readShort();

        if (version == HeaderV1.VERSION) {
            return new HeaderV1(signature, version, type);
        }
        if (version == HeaderV2.VERSION) {
            return new HeaderV2(signature, version, type);
        }

        throw new InvalidHeaderException(String.format("invalid Header : signature(0x%02X), version(0x%02X)", signature, version));
    }

    public HeaderEntity readHeaderEntity(Header header) {
        final byte version = header.getVersion();
        if (version == HeaderV1.VERSION) {
            return HeaderEntity.EMPTY_HEADER_ENTITY;
        }
        if (version == HeaderV2.VERSION) {
            return readHeaderEntity();
        }

        throw new InvalidHeaderException("invalid Header : " + header);
    }

    private HeaderEntity readHeaderEntity() {
        final short headerEntitySize = buffer.readShort();

        if (headerEntitySize < 0 || headerEntitySize > HeaderV2.HEADER_ENTITY_COUNT_MAX_SIZE) {
            throw new InvalidHeaderException("header entity count size is invalid. size : " + headerEntitySize);
        }
        if (headerEntitySize == 0) {
            return HeaderEntity.EMPTY_HEADER_ENTITY;
        }

        final Map<String, String> headerEntity = new HashMap<String, String>(headerEntitySize);
        for (int i = 0 ; i < headerEntitySize ; i++ ) {
            final String key = readString();
            final String value = readString();
            headerEntity.put(key, value);
        }
        return new HeaderEntity(headerEntity);
    }

    private String readString() {
        final short stringLength = buffer.readShort();
        if(!validCheck(stringLength)) {
            throw new InvalidHeaderException("string length is invalid in header data. length : " + stringLength);
        }
        // rewind offset
        buffer.setOffset(buffer.getOffset() - 2);
        return buffer.read2PrefixedString();
    }

    private boolean validCheck(short length) {
        if (length > HeaderV2.HEADER_ENTITY_STRING_MAX_LANGTH || length == 0) {
            return false;
        }

        return true;
    }

    @Override
    public int getOffset() {
        return this.buffer.getOffset();
    }


    @Override
    public int getRemaining() {
        return this.buffer.remaining();
    }


}
