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


import java.util.Collections;
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
            throw new NullPointerException("bytes must not be null");
        }
        return bytes;
    }


    public Header readHeader() {
        final byte signature = buffer.readByte();
        if (signature != Header.SIGNATURE) {
            throw new InvalidHeaderException("invalid signature :"  + signature);
        }

        final byte version = buffer.readByte();
        if (version == HeaderV1.VERSION) {
            return createHeaderV1(signature, version);
        }
        if (version == HeaderV2.VERSION) {
            return createHeaderV2(signature, version);
        }

        throw new InvalidHeaderException(String.format("invalid Header : signature(0x%02X), version(0x%02X)", signature, version));

    }

    private HeaderV2 createHeaderV2(byte signature, byte version) {
        final short type = buffer.readShort();

        final short headerSize = buffer.readShort();
        if (headerSize == 0) {
            return new HeaderV2(signature, version, type, Collections.<String, String>emptyMap());
        }
        if (headerSize > HeaderV2.HEADER_DATA_MAX_SIZE) {
            throw new InvalidHeaderException("header data size exceed the max limit. size : " + headerSize);
        }

        Map<String, String> headerBody = readHeaderBody(headerSize);
        Map<String, String> unmodifiableBody = Collections.unmodifiableMap(headerBody);
        return new HeaderV2(signature, version, type, unmodifiableBody);
    }

    private Map<String, String> readHeaderBody(short headerSize) {
        final Map<String, String> data = new HashMap<String, String>(headerSize);
        for (int i = 0 ; i < headerSize ; i++ ) {
            final String key = readString();
            final String value = readString();
            data.put(key, value);
        }
        return data;
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
        if (length > HeaderV2.HEADER_DATA_STRING_MAX_LANGTH || length == 0) {
            return false;
        }

        return true;
    }

    private HeaderV1 createHeaderV1(byte signature, byte version) {
        final short type = buffer.readShort();
        return new HeaderV1(signature, version, type);
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
