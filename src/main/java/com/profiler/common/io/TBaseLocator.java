package com.profiler.common.io;

import com.profiler.common.dto2.Header;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

public interface TBaseLocator {
    TBase<?, ?> tBaseLookup(short type) throws TException;

//    short typeLookup(TBase<?, ?> tbase) throws TException;

    Header headerLookup(TBase<?, ?> dto) throws TException;
}
