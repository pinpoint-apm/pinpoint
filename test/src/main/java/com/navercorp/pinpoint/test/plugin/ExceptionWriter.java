package com.navercorp.pinpoint.test.plugin;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionWriter {

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
            for (StackTraceElement e : stackTrace) {
                builder.append(e.getClassName());
                builder.append(',');
                builder.append(e.getMethodName());
                builder.append(',');
                builder.append(e.getFileName());
                builder.append(',');
                builder.append(e.getLineNumber());

                builder.append(PluginTestConstants.JUNIT_OUTPUT_DELIMITER);
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
    public static final Pattern pattern = Pattern.compile(System.lineSeparator());

    private String removeLineSeparator(String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(message);
        return matcher.replaceAll("");
    }
}
