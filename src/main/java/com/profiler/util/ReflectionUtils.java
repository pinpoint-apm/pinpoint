package com.profiler.util;

import java.lang.reflect.Field;

public class ReflectionUtils {
    public static Field findField(Class targetClass, String fieldName) {
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field f : declaredFields) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        return null;
    }
}
