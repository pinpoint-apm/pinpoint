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
package com.navercorp.pinpoint.io.header.v2;

import com.navercorp.pinpoint.io.header.Header;

/**
 * The type Header.
 *
 * @author minwoo.jung
 */
final public class HeaderV2 implements Header {

    public static final byte VERSION = 0x20;

    public static final int HEADER_ENTITY_COUNT_MAX_SIZE = 64;
    public static final int HEADER_ENTITY_STRING_MAX_LANGTH = 1024;

    private final short type;

    public HeaderV2(byte signature, byte version, short type) {
        if (signature != Header.SIGNATURE) {
            throw new IllegalArgumentException("invalid signature " + signature);
        }
        if (version != VERSION){
            throw new IllegalArgumentException("invalid version " + version);
        }
        this.type = type;
    }

    @Override
    public byte getSignature() {
        return SIGNATURE;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public short getType() {
        return type;
    }

    @Override
    public String toString() {
        return "HeaderV2{" +
            "signature=" + SIGNATURE +
            ", version=" + VERSION +
            ", type=" + type +
            '}';
    }
}
