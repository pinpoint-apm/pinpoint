package com.navercorp.pinpoint.bootstrap;

/**
 * @author Woonduk Kang(emeroad)
 */
final class ModuleUtils {

    private static final boolean moduleSupport = checkModuleClass();

    private static boolean checkModuleClass() {
        try {
            Class.forName("java.lang.Module", false, null);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static boolean isModuleSupported() {
        return moduleSupport;
    }
}
