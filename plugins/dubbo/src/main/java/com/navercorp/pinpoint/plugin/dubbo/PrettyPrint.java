package com.navercorp.pinpoint.plugin.dubbo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

public class PrettyPrint {
    private static SerializeFilter[] filters = new SerializeFilter[2];
    static {
        SimplePropertyPreFilter spp = new SimplePropertyPreFilter();
        spp.setMaxLevel(5);
        filters[0] = spp;
        filters[1] = new PropertyFilter() {
            @Override
            public boolean apply(Object o, String s, Object o1) {
                return !(String.class.isInstance(o1) && o1.toString().length() > 10) ;
            }
        };

    }

    public static String toString(Object obj) {
        return JSONObject.toJSONString(obj);
    }
    public static String toSimpleString(Object obj) {
        return JSONObject.toJSONString(obj,filters);
    }
}