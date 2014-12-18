package com.navercorp.pinpoint.collector.mapper.thrift;

import org.apache.thrift.TBase;

/**
 * @author hyungil.jeong
 */
public interface ThriftBoMapper<T, F extends TBase<?,?>> {

    T map(F thriftObject);
}
