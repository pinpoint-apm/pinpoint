package com.nhn.pinpoint.profiler.util.bindvalue.converter;

/**
 * @author emeroad
 */
public class NullTypeConverter implements Converter {

    @Override
    public String convert(Object[] args) {
        return "null";
    }
}
