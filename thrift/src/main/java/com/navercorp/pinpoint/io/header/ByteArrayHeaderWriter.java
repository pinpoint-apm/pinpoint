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
import org.apache.thrift.TException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class ByteArrayHeaderWriter implements HeaderWriter {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final Header header;
    private final AutomaticBuffer buffer;

    public ByteArrayHeaderWriter(Header header) {
        if (header == null) {
            throw new NullPointerException("header must not be null.");
        }

        this.buffer = new AutomaticBuffer(4);
        this.header = header;
    }

    @Override
    public byte[] writeHeader() {
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

        return buffer.getBuffer();
    }

    private void writeHeaderV1(HeaderV1 header) throws TException {
        writeHeaderPrefix();
    }

    private void writeHeaderPrefix() {
        buffer.putByte(header.getSignature());
        buffer.putByte(header.getVersion());
        buffer.putShort(header.getType());
    }


    private void writeHeaderV2(HeaderV2 header) throws TException, UnsupportedEncodingException {
        writeHeaderPrefix();

        writeHeaderData(header.getHeaderData());
    }

    private void writeHeaderData(Map<String, String> data) throws TException, UnsupportedEncodingException {
        final int size = data.size();
        if (size >= HeaderV2.HEADER_DATA_MAX_SIZE) {
            throw new InvalidHeaderException("header size is to big. size : " + size);
        }
        buffer.putShort((short)size);
        if (size == 0) {
            return;
        }

        for (Map.Entry<String, String> entry : data.entrySet()) {
            writeString(entry.getKey());
            writeString(entry.getValue());
        }
    }

    private void writeString(String value) throws TException, UnsupportedEncodingException {
        if (!validCheck(value)) {
            throw new InvalidHeaderException("string length is invalid in header data. value : " + value);
        }

        byte[] valueBytes = value.getBytes(UTF_8);
        buffer.put2PrefixedBytes(valueBytes);
    }

    private boolean validCheck(String value) {
        int length = value.length();

        if (length > HeaderV2.HEADER_DATA_STRING_MAX_LANGTH || length == 0) {
            return false;
        }

        return true;
    }
}
