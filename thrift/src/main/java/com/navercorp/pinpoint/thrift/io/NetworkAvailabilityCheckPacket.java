package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TProtocol;

/**
 * @author netspider
 */
public class NetworkAvailabilityCheckPacket implements org.apache.thrift.TBase<NetworkAvailabilityCheckPacket, org.apache.thrift.TFieldIdEnum>, java.io.Serializable, Cloneable, Comparable<NetworkAvailabilityCheckPacket> {

	private static final long serialVersionUID = -1170704876834222604L;
	
	public transient static final byte[] DATA_OK = "OK".getBytes(); 
	
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
	public int compareTo(NetworkAvailabilityCheckPacket o) {
		return 0;
	}
}
