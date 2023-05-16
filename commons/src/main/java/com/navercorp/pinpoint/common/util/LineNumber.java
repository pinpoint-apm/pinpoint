package com.navercorp.pinpoint.common.util;

public final class LineNumber {
    public static final int NO_LINE_NUMBER = 0;
    public static final int LEGACY_NO_LINE_NUMBER = -1;


    public static boolean isNoLineNumber(int lineNumber) {
        if (lineNumber == NO_LINE_NUMBER) {
            return true;
        }
        if (lineNumber == LEGACY_NO_LINE_NUMBER) {
            return true;
        }
        return false;

    }

    public static boolean isLineNumber(int lineNumber) {
        return !isNoLineNumber(lineNumber);
    }

    public static int defaultLineNumber(int lineNumber) {
        if (lineNumber == LEGACY_NO_LINE_NUMBER) {
            return NO_LINE_NUMBER;
        }
        return lineNumber;
    }
}
