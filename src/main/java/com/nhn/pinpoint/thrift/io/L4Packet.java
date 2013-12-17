package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TProtocol;

/**
 * @author emeroad
 */
public class L4Packet implements org.apache.thrift.TBase<L4Packet, org.apache.thrift.TFieldIdEnum>, java.io.Serializable, Cloneable, Comparable<L4Packet> {

    private final Header header;

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
