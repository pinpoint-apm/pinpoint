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
package com.navercorp.pinpoint.thrift.io.header.v1;

import com.navercorp.pinpoint.thrift.io.Header;

import java.util.Collections;
import java.util.Map;

/**
 * @author minwoo.jung
 */
final public class HeaderV1 implements Header {

    public static final byte VERSION = 0x10;

    private final byte signature;
    private final byte version;
    private final short type;

    public HeaderV1(byte signature, byte version, short type) {
        this.signature = signature;
        this.version = version;
        this.type = type;
    }

    public HeaderV1(short type) {
        this.signature = SIGNATURE;
        this.version = VERSION;
        this.type = type;
    }

    @Override
    public byte getSignature() {
        return signature;
    }

    public byte getVersion() {
        return version;
    }

    public short getType() {
        return type;
    }

    @Override
    public Map<String, String> getData() {
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return "Header{" +
            "signature=" + signature +
            ", version=" + version +
            ", type=" + type +
            '}';
    }
}
