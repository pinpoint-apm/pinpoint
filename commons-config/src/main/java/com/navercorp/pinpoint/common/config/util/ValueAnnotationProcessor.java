package com.navercorp.pinpoint.common.config.util;


import com.navercorp.pinpoint.common.config.ConfigurationException;
import com.navercorp.pinpoint.common.config.Value;
import com.navercorp.pinpoint.common.config.util.spring.PropertyPlaceholderHelper;
import com.navercorp.pinpoint.common.util.ModifierUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ValueAnnotationProcessor {

    private final PropertyPlaceholderHelper placeHolderParser =
        new PropertyPlaceholderHelper(PlaceHolder.START, PlaceHolder.END, PlaceHolder.DELIMITER, false);

    public ValueAnnotationProcessor() {
    }

    public void process(Object instance, final Function<String, String> placeholderResolver) throws ConfigurationException {
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

    private void handleFields(Class<?> aClass, Object instance, Function<String, String> placeholderResolver) {
        for (Field field : aClass.getDeclaredFields()) {
            final String value = getValue(field, placeholderResolver);
            if (value != null) {
                injectField(field, instance, value);
            }

        }
    }

    private void handleMethods(Class<?> aClass, Object instance, Function<String, String> placeholderResolver) {
        for (Method method : filterMethod(aClass)) {
            final String value = getValue(method, placeholderResolver);
            if (value != null) {
                injectMethod(method, instance, value);
            }

        }
    }

    private String getValue(AccessibleObject accessibleObject, final Function<String, String> placeholderResolver) {
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
        if (!(method.getParameterCount() == 1)) {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object parse(Class<?> type, String value) {
        if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type, value);
        }

        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == short.class || type == Short.class) {
            return Short.parseShort(value);
        } else if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(value);
        } else if (type == char.class || type == Character.class) {
            return parseChar(value);
        }
        throw new ConfigurationException("Unsupported type:" + type.getName());
    }

    private char parseChar(String value) {
        if (value.length() != 1) {
            throw new IllegalArgumentException("Invalid value:" + value);
        }
        return value.charAt(0);
    }

    private void injectField(Field field, Object target, String value) {
        final Class<?> fieldType = field.getType();

        try {
            final Object parsedValue = parse(fieldType, value);
            if (parsedValue != null) {
                try {
                    setAccessible(field);
                    field.set(target, parsedValue);
                } catch (ReflectiveOperationException e) {
                    throw new ConfigurationException(getFieldName(target, field) + " access error", e);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("injectField error field:" + field + " value:" + value, ex);
        }
    }

    private String getFieldName(Object instance, Member field) {
        return instance.getClass().getSimpleName() + "." + field.getName();
    }

}
