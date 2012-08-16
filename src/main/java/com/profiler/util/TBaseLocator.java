package com.profiler.util;

import com.profiler.dto.Header;
import org.apache.thrift.TBase;

public interface TBaseLocator {
    TBase<?, ?> tBaseLookup(short type);
    short typeLookup(TBase<?, ?> tbase);
}
