package com.profiler.util;

import com.profiler.dto.Header;
import org.apache.thrift.TBase;

public interface TBaseLocator {
    TBase lookup(Header header);
}
