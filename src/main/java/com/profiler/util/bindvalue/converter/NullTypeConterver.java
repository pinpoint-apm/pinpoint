package com.profiler.util.bindvalue.converter;

public class NullTypeConterver implements Converter {

    @Override
    public String convert(Object[] args) {
        return "null";
    }
}
