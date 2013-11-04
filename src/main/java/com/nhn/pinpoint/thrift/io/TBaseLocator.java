package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

/**
 * @author emeroad
 */
public interface TBaseLocator {
    TBase<?, ?> tBaseLookup(short type) throws TException;

//    short typeLookup(TBase<?, ?> tbase) throws TException;

    Header headerLookup(TBase<?, ?> dto) throws TException;
}
