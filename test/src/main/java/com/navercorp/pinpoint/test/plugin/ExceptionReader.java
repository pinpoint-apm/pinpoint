package com.navercorp.pinpoint.test.plugin;


import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.tinylog.TaggedLogger;

import java.util.Arrays;
import java.util.List;

public class ExceptionReader {
    private TaggedLogger logger = TestLogger.getLogger();

    public Exception read(String exceptionClass, String message, List<String> traceInText) {
        StackTraceElement[] stackTrace = new StackTraceElement[traceInText.size()];
        for (int i = 0; i < traceInText.size(); i++) {
            String trace = traceInText.get(i);
            if (PluginTestConstants.CAUSED_DELIMITER.equals(trace)) {
                final String parsedExceptionClass = traceInText.get(i + 1);
                final String parsedMessage = traceInText.get(i + 2);
                final List<String> sublist = traceInText.subList(i + 3, traceInText.size());

                Exception cause = read(parsedExceptionClass, parsedMessage, sublist);
                return newPluginException(exceptionClass, message, cause, Arrays.copyOf(stackTrace, i));
            }

            final String[] tokens = trace.split(",");
            if (tokens.length != 4) {
                logger.warn("Unexpected trace string: {}", trace);
                stackTrace[i] = new StackTraceElement(trace, "", null, -1);
            } else {
                stackTrace[i] = new StackTraceElement(tokens[0], tokens[1], tokens[2], Integer.parseInt(tokens[3]));
            }

        }
        return newPluginException(exceptionClass, message, null, stackTrace);
    }


    private Exception newPluginException(String exceptionClass, String message, Throwable cause, StackTraceElement[] stackTrace) {
        PinpointPluginTestException testException = new PinpointPluginTestException(exceptionClass, message, cause);
        testException.setStackTrace(stackTrace);
        return testException;
    }
}
