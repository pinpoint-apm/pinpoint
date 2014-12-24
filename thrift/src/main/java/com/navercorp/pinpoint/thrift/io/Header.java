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

/**
 * @author emeroad
 */
public class Header {

    public static final byte SIGNATURE = (byte) 0xef;

    public static final int HEADER_SIZE = 4;

    private byte signature = SIGNATURE;
    private byte version = 0x10;
    private short type = 0;

    public Header() {
    }

    public Header(byte signature, byte version, short type) {
        this.signature = signature;
        this.version = version;
        this.type = type;
    }

    public byte getSignature() {
        return signature;
    }

    public void setSignature(byte signature) {
        this.signature = signature;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
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

