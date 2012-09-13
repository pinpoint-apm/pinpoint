package com.profiler.common.util;

import com.profiler.common.dto.Header;

import org.apache.thrift.TBase;

public interface TBaseLocator {
    TBase<?, ?> tBaseLookup(short type);
    short typeLookup(TBase<?, ?> tbase);
}
