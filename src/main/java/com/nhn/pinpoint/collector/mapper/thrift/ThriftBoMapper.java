package com.nhn.pinpoint.collector.mapper.thrift;

import org.apache.thrift.TBase;

/**
 * @author hyungil.jeong
 */
public interface ThriftBoMapper<T, F extends TBase<?,?>> {

    public T map(F thriftObject);
}
