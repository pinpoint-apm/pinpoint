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
package com.navercorp.pinpoint.io.header.v1;

import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.InvalidHeaderException;

/**
 * @author minwoo.jung
 */
final public class HeaderV1 implements Header {

    public static final byte VERSION = 0x10;

//   skip constant field
//    private final byte signature;
//    private final byte version;
    private final short type;

    public HeaderV1(byte signature, byte version, short type) {
        if (signature != Header.SIGNATURE) {
            throw new InvalidHeaderException("invalid signature " + signature);
        }
        if (version != VERSION){
            throw new InvalidHeaderException("invalid version " + version);
        }
        this.type = type;
    }

    public HeaderV1(short type) {
        this.type = type;
    }

    @Override
    public byte getSignature() {
        return Header.SIGNATURE;
    }

    public byte getVersion() {
        return VERSION;
    }

    public short getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Header{" +
            "signature=" + SIGNATURE +
            ", version=" + VERSION +
            ", type=" + type +
            '}';
    }
}
