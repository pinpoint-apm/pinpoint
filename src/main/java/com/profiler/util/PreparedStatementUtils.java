package com.profiler.util;


import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class PreparedStatementUtils {

    private static final Pattern BIND_SETTER = Pattern.compile("set[A-Z]([a-zA-Z]+)");

    private static final List<Method> bindMethod;

    static {
        bindMethod = findBindVariableSetMethod0();
    }

    public static List<Method> findBindVariableSetMethod() {
        return bindMethod;
    }

    public static List<Method> findBindVariableSetMethod(BindVariableFilter filter) {
        List<Method> temp = new ArrayList<Method>(bindMethod.size());
        for (Method method : bindMethod) {
            if (filter.filter(method)) {
                temp.add(method);
            }
        }
        return temp;
    }

    static List<Method> findBindVariableSetMethod0() {
        Method[] methods = PreparedStatement.class.getDeclaredMethods();
        List<Method> bindMethod = new LinkedList<Method>();
        for (Method method : methods) {
            if (isSetter(method.getName())) {
                Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterTypes.length < 2) {
                    continue;
                }
                if (parameterTypes[0] != int.class) {
                    continue;
                }
                if (method.getReturnType() != void.class) {
                    continue;
                }
                if (method.getExceptionTypes().equals(SQLException.class)) {
                    continue;
                }
                bindMethod.add(method);
            }
        }
        return Collections.unmodifiableList(bindMethod);
    }


    public static boolean isSetter(String name) {
        if (name == null) {
            return false;
        }
        return BIND_SETTER.matcher(name).matches();
    }
}
