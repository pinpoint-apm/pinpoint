package com.navercorp.pinpoint.bootstrap.config.util;

import com.navercorp.pinpoint.bootstrap.config.ConfigurationException;
import com.navercorp.pinpoint.bootstrap.config.Value;
import com.navercorp.pinpoint.bootstrap.util.spring.PropertyPlaceholderHelper;
import com.navercorp.pinpoint.common.util.ModifierUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class ValueAnnotationProcessor {
    private final static Map<Class<?>, ParameterParser> parameterMap = newFieldInjectorMap();

    private final PropertyPlaceholderHelper placeHolderParser =
        new PropertyPlaceholderHelper(PlaceHolder.START, PlaceHolder.END, PlaceHolder.DELIMITER, false);

    private static Map<Class<?>, ParameterParser> newFieldInjectorMap() {
        Map<Class<?>, ParameterParser> map = new IdentityHashMap<>();
//        put(map, new EnumParameterParser());

        put(map, new StringParameterParser());

        put(map, new IntegerParameterParser());
        put(map, new LongParameterParser());
        put(map, new BooleanParameterParser());
        put(map, new DoubleParameterParser());
        put(map, new FloatParameterParser());
        put(map, new ShortParameterParser());
        put(map, new ByteParameterParser());
        put(map, new CharParameterParser());

        return map;
    }

    private static void put(Map<Class<?>, ParameterParser> map, ParameterParser parameterParser) {
        Class<?>[] fieldTypes = parameterParser.getTypes();
        for (Class<?> fieldType : fieldTypes) {
            map.put(fieldType, parameterParser);
        }
    }

    public ValueAnnotationProcessor() {
    }

    public void process(Object instance, final Properties properties) throws ConfigurationException {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(properties, "properties");
        PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver = new PropertyPlaceholderHelper.PlaceholderResolver() {
            public String resolvePlaceholder(String placeholderName) {
                return properties.getProperty(placeholderName);
            }
        };
        process(instance, placeholderResolver);
    }

    public void process(Object instance, final PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver) throws ConfigurationException {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(placeholderResolver, "placeholderResolver");

        final Class<?> aClass = instance.getClass();

        final String packageName = aClass.getPackage().getName();
        if (!packageName.startsWith("com.navercorp.pinpoint")) {
            throw new IllegalAccessError("Access violation package:" + packageName);
        }
        handleFields(aClass, instance, placeholderResolver);
        handleMethods(aClass, instance, placeholderResolver);

    }

    private void handleFields(Class<?> aClass, Object instance, PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver) {
        for (Field field : aClass.getDeclaredFields()) {
            final String value = getValue(field, placeholderResolver);
            if (value != null) {
                injectField(field, instance, value);
            }

        }
    }

    private void handleMethods(Class<?> aClass, Object instance, PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver) {
        for (Method method : filterMethod(aClass)) {
            final String value = getValue(method, placeholderResolver);
            if (value != null) {
                injectMethod(method, instance, value);
            }

        }
    }

    private String getValue(AccessibleObject accessibleObject, final PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver) {
        String rawKey = getValueFromAnnotation(accessibleObject);
        if (rawKey == null) {
            return null;
        }

        try {
            return this.placeHolderParser.replacePlaceholders(rawKey, placeholderResolver);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<Method> filterMethod(Class<?> clazz) {
        List<Method> list = new ArrayList<>();
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (filterMethod(declaredMethod)) {
                list.add(declaredMethod);
            }
        }
        return list;
    }

    private boolean filterMethod(Method method) {
        if (!method.isAnnotationPresent(Value.class)) {
            return false;
        }

        if (!checkModifier(method)) {
            return false;
        }
        if (!method.getName().startsWith("set")) {
            return false;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (!(parameterTypes.length == 1)) {
            return false;
        }
        return true;
    }

    private boolean checkModifier(Method method) {
        final int mod = method.getModifiers();
        if (Modifier.isPublic(mod)) {
            return true;
        }
        if (ModifierUtils.isPackage(mod)) {
            return true;
        }
        return false;
    }


    private String getValueFromAnnotation(AccessibleObject accessibleObject) {
        Value valueAnnotation = accessibleObject.getAnnotation(Value.class);
        if (valueAnnotation == null) {
            return null;
        }
        return valueAnnotation.value();
    }


    private void setAccessible(AccessibleObject accessibleObject) {
        if (!accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
    }

    private void injectMethod(Method method, Object target, String value) {
        final Class<?> parameterType = method.getParameterTypes()[0];

        final Object parsedValue = parse(parameterType, value);
        if (parsedValue != null) {
            try {
                setAccessible(method);
                method.invoke(target, parsedValue);
            } catch (ReflectiveOperationException e) {
                throw new ConfigurationException(getFieldName(target, method) + " access error", e);
            }
        } else {
            throw new ConfigurationException("unsupported data type :" + getFieldName(target, method));
        }
    }

    private Object parse(Class<?> parameterType, String value) {
        if (parameterType.isEnum()) {
            return parseEnum((Class<Enum>) parameterType, value);
        }

        final ParameterParser parameterParser = parameterMap.get(parameterType);
        if (parameterParser == null) {
            throw new ConfigurationException("Unsupported type:" + parameterType);
        }
        return parameterParser.parse(value);
    }

    private void injectField(Field field, Object target, String value) {
        final Class<?> fieldType = field.getType();

        final Object parsedValue = parse(fieldType, value);
        if (parsedValue != null) {
            try {
                setAccessible(field);
                field.set(target, parsedValue);
            } catch (ReflectiveOperationException e) {
                throw new ConfigurationException(getFieldName(target, field) + " access error", e);
            }
        }
    }

    private String getFieldName(Object instance, Member field) {
        return instance.getClass().getSimpleName() + "." + field.getName();
    }

    private Object parseEnum(Class<Enum> type, String value) {
        return Enum.valueOf(type, value);
    }


    public interface ParameterParser {
        Class<?>[] getTypes();

        Object parse(String value);
    }


    private static class StringParameterParser implements ParameterParser {
        @Override
        public Class<?>[] getTypes() {
            return new Class[]{String.class};
        }

        @Override
        public Object parse(String value) {
            return value;
        }

    }


    private static class IntegerParameterParser implements ParameterParser {
        @Override
        public Class<?>[] getTypes() {
            return new Class[]{int.class, Integer.class};
        }

        @Override
        public Object parse(String value) {
            return Integer.parseInt(value);
        }

    }

    private static class LongParameterParser implements ParameterParser {
        @Override
        public Class<?>[] getTypes() {
            return new Class[]{long.class, Long.class};
        }

        @Override
        public Object parse(String value) {
            return Long.parseLong(value);
        }

    }

    private static class BooleanParameterParser implements ParameterParser {
        @Override
        public Class<?>[] getTypes() {
            return new Class[]{boolean.class, Boolean.class};
        }

        @Override
        public Object parse(String value) {
            return Boolean.parseBoolean(value);
        }

    }

    private static class DoubleParameterParser implements ParameterParser {
        @Override
        public Class<?>[] getTypes() {
            return new Class[]{double.class, Double.class};
        }

        @Override
        public Object parse(String value) {
            return Double.parseDouble(value);
        }

    }

    private static class FloatParameterParser implements ParameterParser {
        @Override
        public Class<?>[] getTypes() {
            return new Class[]{float.class, Float.class};
        }

        @Override
        public Object parse(String value) {
            return Float.parseFloat(value);
        }

    }

    private static class ShortParameterParser implements ParameterParser {
        @Override
        public Class<?>[] getTypes() {
            return new Class[]{short.class, Short.class};
        }

        @Override
        public Object parse(String value) {
            return Short.parseShort(value);
        }

    }

    private static class ByteParameterParser implements ParameterParser {
        @Override
        public Class<?>[] getTypes() {
            return new Class[]{byte.class, Byte.class};
        }

        @Override
        public Object parse(String value) {
            return Byte.parseByte(value);
        }

    }

    private static class CharParameterParser implements ParameterParser {
        @Override
        public Class<?>[] getTypes() {
            return new Class[]{char.class, Character.class};
        }

        @Override
        public Object parse(String value) {
            return parseChar(value);
        }

        private char parseChar(String value) {
            if (value.length() != 1) {
                throw new IllegalArgumentException("Invalid value:" + value);
            }
            return value.charAt(0);
        }

    }


}
