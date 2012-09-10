package com.profiler.util.bindvalue;

import com.profiler.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class BindValueConverter {
    public static final Map<String, Converter> convertermap = new HashMap<String, Converter>() ;

    public String bindValueToString(String methodName, Object[] args) {
        Converter converter = convertermap.get(methodName);
        if (converter == null) {
            return "";
        }
        return converter.convert(args);
    }

    interface Converter {
        String convert(Object[] args);
    }

    class CommonConverter implements Converter {
        @Override
        public String convert(Object[] args) {
            if(args == null) {
                return "null";
            }
            if (args.length == 1) {
                return StringUtils.toString(args[1]);
            }
            else if(args.length == 2) {
                return StringUtils.toString(args[0]) + ":" + StringUtils.toString(args[1]);
            }
            else if(args.length == 3) {
                return StringUtils.toString(args[0]) + ":" + StringUtils.toString(args[1]);
            }
            return "error";
        }
    }
}
