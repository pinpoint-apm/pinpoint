package com.navercorp.pinpoint.test.plugin;


import com.navercorp.pinpoint.test.plugin.util.ArrayUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionWriter {
    private static final Pattern PATTERN = Pattern.compile(System.lineSeparator());

    public String write(String header, Throwable t) {
        StringBuilder builder = new StringBuilder(256);

        builder.append(header);
        builder.append(PluginTestConstants.JUNIT_OUTPUT_DELIMITER);

        while (true) {
            builder.append(t.getClass().getName());
            builder.append(PluginTestConstants.JUNIT_OUTPUT_DELIMITER);
            builder.append(removeLineSeparator(t.getMessage()));
            builder.append(PluginTestConstants.JUNIT_OUTPUT_DELIMITER);
            final StackTraceElement[] stackTrace = t.getStackTrace();
            // workaround for exception without stacktrace
            if (ArrayUtils.isEmpty(stackTrace)) {
                StackTraceElement empty = new StackTraceElement("EMPTY_CLASS", "empty", null, 0);
                writeStackTrace(builder, empty);
                builder.append(PluginTestConstants.JUNIT_OUTPUT_DELIMITER);
            } else {
                for (StackTraceElement e : stackTrace) {
                    writeStackTrace(builder, e);
                    builder.append(PluginTestConstants.JUNIT_OUTPUT_DELIMITER);
                }
            }

            Throwable cause = t.getCause();

            if (cause == null || t == cause) {
                break;
            }

            t = cause;
            builder.append(PluginTestConstants.CAUSED_DELIMITER);
            builder.append(PluginTestConstants.JUNIT_OUTPUT_DELIMITER);
        }

        return builder.toString();
    }

    public static void writeStackTrace(StringBuilder writer, StackTraceElement stackTraceElement) {
        writer.append(stackTraceElement.getClassName());
        writer.append(',');
        writer.append(stackTraceElement.getMethodName());
        writer.append(',');
        writer.append(stackTraceElement.getFileName());
        writer.append(',');
        writer.append(stackTraceElement.getLineNumber());
    }


    private String removeLineSeparator(String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(message);
        return matcher.replaceAll("");
    }
}
