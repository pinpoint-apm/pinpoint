package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MetaObject<R> {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final String methodName;
    private final Class<?>[] args;
    private final R defaultReturnValue;

    // 이것을 class loading시 정적 타임에서 생성해 둘수 없는가?
    private Method methodRef;



    public MetaObject(String methodName, Class... args) {
        this.methodName = methodName;
        this.args = args;
        this.defaultReturnValue = null;
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
            final Class<?> aClass = target.getClass();
            method = findMethod(aClass);
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

    private Method findMethod(Class<?> aClass) {
        try {
            final Method method = aClass.getMethod(this.methodName, this.args);
            if (!method.isAccessible()) {
                // package등과 같이 access 제한이 걸려 있을 경우 강 푼다.
                method.setAccessible(true);
            }
            return method;
        } catch (NoSuchMethodException e) {
            logger.warn("{} not found class:{} Caused:{}", new Object[] { this.methodName, aClass, e.getMessage(), e });
            return null;
        }
    }

}
