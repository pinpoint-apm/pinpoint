/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.io.header;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;

import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class ByteArrayHeaderWriter implements HeaderWriter {

    private final Header header;
    private final AutomaticBuffer buffer;
    private final HeaderEntity headerEntity;

    public ByteArrayHeaderWriter(Header header, HeaderEntity headerEntity) {
        this.header = Objects.requireNonNull(header, "header");
        this.buffer = new AutomaticBuffer(32);
        this.headerEntity = headerEntity;
    }

    @Override
    public byte[] writeHeader() {
        byte version = header.getVersion();

        if (version == HeaderV1.VERSION) {
            writeHeaderV1();
        } else if (version == HeaderV2.VERSION) {
            writeHeaderV2();
        } else {
            throw new InvalidHeaderException("can not find header version. header : " + header);
        }

        return buffer.getBuffer();
    }

    private void writeHeaderV1() {
        writeHeaderPrefix();
    }

    private void writeHeaderPrefix() {
        buffer.putByte(header.getSignature());
        buffer.putByte(header.getVersion());
        buffer.putShort(header.getType());
    }


    private void writeHeaderV2() {
        writeHeaderPrefix();
        writeHeaderEntity();
    }

    private void writeHeaderEntity() {
        Map<String, String> headerEntityData = headerEntity.getEntityAll();
        final int size = headerEntityData.size();
        if (size >= HeaderV2.HEADER_ENTITY_COUNT_MAX_SIZE) {
            throw new InvalidHeaderException("header size is to big. size : " + size);
        }

        buffer.putShort((short)size);

        if (size == 0) {
            return;
        }
        for (Map.Entry<String, String> entry : headerEntityData.entrySet()) {
            writeString(entry.getKey());
            writeString(entry.getValue());
        }
    }

    private void writeString(String value) {
        if (!validCheck(value)) {
            throw new InvalidHeaderException("string length is invalid in header data. value : " + value);
        }

        buffer.put2PrefixedString(value);
    }

    private boolean validCheck(String value) {
        int length = value.length();

        if (length > HeaderV2.HEADER_ENTITY_STRING_MAX_LANGTH || length == 0) {
            return false;
        }

        return true;
    }
}
