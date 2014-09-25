package com.nhn.pinpoint.profiler.util.bindvalue.converter;

import com.nhn.pinpoint.bootstrap.util.StringUtils;

/**
 * @author emeroad
 */
public class ClassNameConverter implements Converter {
    @Override
    public String convert(Object[] args) {
        if (args == null) {
            return "null";
        }
        if (args.length == 2) {
            return StringUtils.drop(getClassName(args[1]));
        } else if(args.length == 3) {
            // 3일때의 추가 처리?
            return StringUtils.drop(getClassName(args[1]));
        }
        return "error";
    }

    private String getClassName(Object args) {
        if (args == null) {
            return "null";
        }
        return args.getClass().getName();
    }
}
