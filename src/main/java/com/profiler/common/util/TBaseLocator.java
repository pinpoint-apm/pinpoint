package com.profiler.common.util;

import org.apache.thrift.TBase;

public interface TBaseLocator {
    TBase<?, ?> tBaseLookup(short type);
    short typeLookup(TBase<?, ?> tbase);
}
