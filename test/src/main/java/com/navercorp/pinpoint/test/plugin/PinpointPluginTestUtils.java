package com.navercorp.pinpoint.test.plugin;

import java.lang.reflect.Method;

/**
 * @author Taejin Koo
 */
public final class PinpointPluginTestUtils {

    public static String getTestDescribe(Method method) {
        if (method == null) {
            return "Method null";
        }

        StringBuilder describe = new StringBuilder("Method ");
        describe.append(method.getName());

        if (method.getDeclaringClass() != null) {
            describe.append("(");
            describe.append(method.getDeclaringClass().getName());
            describe.append(")");
        }

        return describe.toString();
    }

}
