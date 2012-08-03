package com.profiler.util;

import com.profiler.dto.Header;
import org.apache.thrift.TBase;

public interface TBaseSelector {
    TBase getSelect(Header header);
}
