package com.profiler.util;

import com.profiler.dto.*;
import org.apache.thrift.TBase;

public class DefaultTBaseLocator implements TBaseLocator{
    @Override
    public TBase lookup(Header header) {
        short type = header.getType();
        switch (type) {
            case 10:
                return new JVMInfoThriftDTO();
            case 20:
                return new RequestDataListThriftDTO();
            case 30:
                return new RequestThriftDTO();
        }
        throw new IllegalArgumentException("Unsupported type:"  + type + " " + header);
    }
}
