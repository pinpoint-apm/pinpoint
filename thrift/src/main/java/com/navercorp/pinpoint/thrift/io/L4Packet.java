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

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TProtocol;

/**
 * @author emeroad
 */
public class L4Packet implements org.apache.thrift.TBase<L4Packet, org.apache.thrift.TFieldIdEnum>, java.io.Serializable, Cloneable, Comparable<L4Packet> {

    private final transient Header header;

    public L4Packet(Header header) {
        this.header = header;
    }

    public Header getHeader() {
        return header;
    }

    @Override
    public void read(TProtocol tProtocol) throws TException {
    }

    @Override
    public void write(TProtocol tProtocol) throws TException {
    }

    @Override
    public TFieldIdEnum fieldForId(int i) {
        return null;
    }

    @Override
    public boolean isSet(TFieldIdEnum tFieldIdEnum) {
        return false;
    }

    @Override
    public Object getFieldValue(TFieldIdEnum tFieldIdEnum) {
        return null;
    }

    @Override
    public void setFieldValue(TFieldIdEnum tFieldIdEnum, Object o) {
    }

    @Override
    public TBase deepCopy() {
        return null;
    }

    @Override
    public void clear() {
    }

    @Override
    public int compareTo(L4Packet o) {
        return 0;
    }
}
