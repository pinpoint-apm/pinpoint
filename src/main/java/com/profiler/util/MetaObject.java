package com.profiler.util;

import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MetaObject<R> {

    private final Logger logger = LoggerFactory.getLogger(MetaObject.class.getName());

    private String methodName;
    private Class[] args;
    // 이것을 class loading시 정적 타임에서 생성해 둘수 없는가?
    private Method methodRef;

    private R defaultReturnValue = null;

    public MetaObject(String methodName, Class... args) {
        this.methodName = methodName;
        this.args = args;
    }

    public MetaObject(R defaultReturnValue, String methodName, Class... args) {
        this.methodName = methodName;
        this.args = args;
        this.defaultReturnValue = defaultReturnValue;
    }

    public R invoke(Object target, Object... args) {
        if (target == null) {
            return defaultReturnValue;
        }

        Method method = this.methodRef;
        if (method == null) {
            // 멀티쓰레드에서 중복 엑세스해도 별 문제 없을것임.
            Class aClass = target.getClass();
            method = getMethod(aClass);
            this.methodRef = method;
        }
        return invoke(method, target, args);
    }

    private R invoke(Method method, Object target, Object[] args) {
        if (method == null) {
            return defaultReturnValue;
        }
        try {
            return (R) method.invoke(target, args);
        } catch (IllegalAccessException e) {
            logger.warn("{} invoke fail", this.methodName, e);
            return defaultReturnValue;
        } catch (InvocationTargetException e) {
            logger.warn("{} invoke fail", this.methodName, e);
            return defaultReturnValue;
        }
    }

    private Method getMethod(Class aClass) {
        try {
            return aClass.getMethod(this.methodName, this.args);
        } catch (NoSuchMethodException e) {
            logger.warn("{} not found cls:{} Caused:{}", new Object[] { this.methodName, aClass, e.getMessage(), e });
            return null;
        }
    }

}
