package com.profiler.util.bindvalue.converter;

import com.profiler.util.StringUtils;

public class SimpleTypeConverter implements Converter {
    @Override
    public String convert(Object[] args) {
        if (args == null) {
            return "null";
        }
        if (args.length == 2) {
            String str = StringUtils.toString(args[0]) + ":" + StringUtils.toString(args[1]);
            return StringUtils.drop(str);
        } else if (args.length == 3) {
            // TODO 3일때 추가 처리?
            String str = StringUtils.toString(args[0]) + ":" + StringUtils.toString(args[1]);
            return StringUtils.drop(str);
        }
        return "error";
    }
}
