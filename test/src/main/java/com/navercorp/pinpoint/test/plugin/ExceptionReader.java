package com.navercorp.pinpoint.test.plugin;


import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.tinylog.TaggedLogger;

import java.util.Arrays;
import java.util.List;

public class ExceptionReader {
    private static final String CAUSED_DELIMITER = PluginTestConstants.CAUSED_DELIMITER;

    private final TaggedLogger logger = TestLogger.getLogger();

    public Exception read(String exceptionClass, String message, List<String> traceInText) {
        StackTraceElement[] stackTrace = new StackTraceElement[traceInText.size()];
        for (int i = 0; i < traceInText.size(); i++) {
            String trace = traceInText.get(i);
            if (CAUSED_DELIMITER.equals(trace)) {
                 final String parsedExceptionClass = traceInText.get(i + 1);
                // no stacktrace
//                if (traceInText.size() > i + 2) {
                    final String parsedMessage = traceInText.get(i + 2);
                    final List<String> sublist = traceInText.subList(i + 3, traceInText.size());

                    Exception cause = read(parsedExceptionClass, parsedMessage, sublist);
                    return newPluginException(exceptionClass, message, cause, Arrays.copyOf(stackTrace, i));
//                } else {
//                    PinpointPluginTestException noStackTrace = new PinpointPluginTestException(parsedExceptionClass, null, null);
//                    return newPluginException(exceptionClass, message, noStackTrace, Arrays.copyOf(stackTrace, i));
//                }
            }
            stackTrace[i] = parseStackTraceElement(trace);
        }
        return newPluginException(exceptionClass, message, null, stackTrace);
    }

    private StackTraceElement parseStackTraceElement(String stackTraceElement) {
        final String[] tokens = stackTraceElement.split(",");
        if (tokens.length == 4) {
            return new StackTraceElement(tokens[0], tokens[1], tokens[2], Integer.parseInt(tokens[3]));
        } else {
            logger.warn("Unexpected trace string: {}", stackTraceElement);
            return new StackTraceElement(stackTraceElement, "", null, -1);
        }
    }

    private Exception newPluginException(String exceptionClass, String message, Throwable cause, StackTraceElement[] stackTrace) {
        PinpointPluginTestException testException = new PinpointPluginTestException(exceptionClass, message, cause);
        testException.setStackTrace(stackTrace);
        return testException;
    }
}
