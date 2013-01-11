package com.profiler.common.util;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

public interface TBaseLocator {
    TBase<?, ?> tBaseLookup(short type) throws TException;

    short typeLookup(TBase<?, ?> tbase) throws TException;
}
