package com.navercorp.pinpoint.rpc.util;

import java.util.List;

public final class ListUtils {

    private ListUtils() {
    }

    public static <V> boolean addIfValueNotNull(List<V> list, V value) {
        if (value == null) {
            return false;
        }

        return list.add(value);
    }
    
    public static <V> boolean addAllIfAllValuesNotNull(List<V> list, V[] values) {
        if (values == null) {
            return false;
        }
        
        for (V value : values) {
            if (value == null) {
                return false;
            }
        }
        
        for (V value : values) {
            list.add(value);
        }
        
        return true;
    }
    
    public static <V> void addAllExceptNullValue(List<V> list, V[] values) {
        if (values == null) {
            return;
        }
        
        for (V value : values) {
            addIfValueNotNull(list, value);
        }
    }
    
}
