/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.Statement;
import org.tinylog.TaggedLogger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.JUNIT_OUTPUT_DELIMITER;

/**
 * @author Jongho Moon
 *
 */
public class PinpointPluginTestStatement extends Statement {
    public static final String JUNIT_OUTPUT_DELIMITER_REGEXP = Pattern.quote(JUNIT_OUTPUT_DELIMITER);

    private final TaggedLogger logger = TestLogger.getLogger();

    private final PinpointPluginTestRunner runner;
    private final RunNotifier notifier;
    private final PinpointPluginTestInstance testCase;
    private final PluginTestContext context;
    private final Result result = new Result();
    
    public PinpointPluginTestStatement(PinpointPluginTestRunner runner, RunNotifier notifier, PluginTestContext context, PinpointPluginTestInstance testCase) {
        this.runner = runner;
        this.context = context;
        this.testCase = testCase;
        this.notifier = notifier;
        this.notifier.addListener(result.createListener());
    }
    
    @Override
    public void evaluate() throws Throwable {
        Description parentDescription = runner.getDescription();

        try {
            Scanner scanner = testCase.startTest();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.endsWith("\\r")) {
                    line = line.substring(0, line.length() - 2);
                }

                if (line.startsWith(JUNIT_OUTPUT_DELIMITER)) {
                    System.out.println(line);
                    String[] tokens = line.split(JUNIT_OUTPUT_DELIMITER_REGEXP);
                    String event = tokens[1];
    
                    if ("testRunStarted".equals(event)) {
                        notifier.fireTestRunStarted(parentDescription);
                    } else if ("testRunFinished".equals(event)) {
                        notifier.fireTestRunFinished(result);
                        break;
                    } else if ("testStarted".equals(event)) {
                        Description ofTest = findDescription(parentDescription, tokens[2]);
                        notifier.fireTestStarted(ofTest);
                    } else if ("testFinished".equals(event)) {
                        Description ofTest = findDescription(parentDescription, tokens[2]);
                        notifier.fireTestFinished(ofTest);
                    } else if ("testFailure".equals(event)) {
                        List<String> stackTrace = slice(tokens);

                        Failure failure = toFailure(parentDescription, tokens[2], tokens[3], tokens[4], stackTrace);
                        notifier.fireTestFailure(failure);
                    } else if ("testAssumptionFailure".equals(event)) {
                        List<String> stackTrace = slice(tokens);
                        Failure failure = toFailure(parentDescription, tokens[2], tokens[3], tokens[4], stackTrace);
                        notifier.fireTestAssumptionFailed(failure);
                    } else if ("testIgnored".equals(event)) {
                        Description ofTest = findDescription(parentDescription, tokens[2]);
                        notifier.fireTestIgnored(ofTest);
                    }
                } else {
                    System.out.println(line);
                }
            }
        } catch (Throwable t) {
            logger.error(t, "Failed to execute test");
            throw t;
        } finally {
            testCase.endTest();
        }
    }

    static List<String> slice(String[] tokens) {
        if (tokens.length > 5) {
            String[] copy = Arrays.copyOfRange(tokens, 5, tokens.length - 1);
            return Arrays.asList(copy);
        }
        return Collections.emptyList();
    }

    private Description findDescription(Description parentDescription, String displayName) {
        if (displayName.equals(parentDescription.getDisplayName())) {
            return parentDescription;
        }
        
        for (Description desc : parentDescription.getChildren()) {
            Description found = findDescription(desc, displayName);
            
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }
    
    private Failure toFailure(Description parentDescription, String displayName, String exceptionClass, String message, List<String> trace) {
        Description desc = findDescription(parentDescription, displayName);
        Exception exception = toException(message, exceptionClass, trace);
        Failure failure = new Failure(desc, exception);
        
        return failure;
    }

    private final ExceptionReader reader = new ExceptionReader();

    private Exception toException(String message, String exceptionClass, List<String> traceInText) {
        return reader.read(exceptionClass, message, traceInText);
    }
}
