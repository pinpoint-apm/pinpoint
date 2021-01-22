package com.navercorp.pinpoint.common.util;

import java.lang.reflect.Modifier;

public final class ModifierUtils {
    private ModifierUtils() {
    }

    public static boolean isPackage(int mod) {
        return (mod & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED)) == 0;
    }
}
