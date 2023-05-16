package com.navercorp.pinpoint.common.util;

public final class ArrayArgumentUtils {
    private ArrayArgumentUtils() {
    }

    public static <T> T getArgument(Object[] args, int index, Class<T> type) {
        return getArgument(args, index, type, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getArgument(Object[] args, int index, Class<T> type, T defaultValue) {
        if (args == null) {
            return defaultValue;
        }
        final Object arg = getArg(args, index);
        if (type.isInstance(arg)) {
            return (T) arg;
        }
        return defaultValue;
    }

    private static Object getArg(Object[] args, int index) {
        final int length = args.length;
        if (index >= 0 && length > index) {
            return args[index];
        }
        return null;
    }

}
