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

import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TProtocol;

import java.io.UnsupportedEncodingException;

/**
 * @author netspider
 */
public class NetworkAvailabilityCheckPacket implements org.apache.thrift.TBase<NetworkAvailabilityCheckPacket, org.apache.thrift.TFieldIdEnum>, java.io.Serializable, Cloneable, Comparable<NetworkAvailabilityCheckPacket> {

    private static final long serialVersionUID = -1170704876834222604L;

    public transient static final byte[] DATA_OK = getBytes("OK");

    private static byte[] getBytes(String str) {
        if (str == null) {
            throw new NullPointerException("str");
        }
        try {
            return str.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding error. Caused:" + e.getMessage(), e);
        }
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
    public NetworkAvailabilityCheckPacket deepCopy() {
        return null;
    }

    @Override
    public void clear() {
    }

    @Override
    public int compareTo(NetworkAvailabilityCheckPacket o) {
        return 0;
    }
}
