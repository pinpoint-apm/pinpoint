package com.navercorp.pinpoint.test.plugin.junit5.descriptor;

public class DescriptorUtils {
    public static String generateDisplayNameForClass(Class<?> testClass) {
        String name = testClass.getName();
        int lastDot = name.lastIndexOf('.');
        return name.substring(lastDot + 1);
    }
}
