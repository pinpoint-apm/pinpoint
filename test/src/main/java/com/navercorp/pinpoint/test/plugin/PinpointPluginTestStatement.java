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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.Statement;

import com.navercorp.pinpoint.common.util.SystemProperty;

import static com.navercorp.pinpoint.test.plugin.PinpointPluginTestConstants.*;

/**
 * @author Jongho Moon
 *
 */
public class PinpointPluginTestStatement extends Statement {
    private static final String JUNIT_OUTPUT_DELIMITER_REGEXP = Pattern.quote(JUNIT_OUTPUT_DELIMITER);

    private final PinpointPluginTestRunner runner;
    private final RunNotifier notifier;
    private final PinpointPluginTestInstance testCase;
    private final PinpointPluginTestContext context;
    private final Result result = new Result();
    
    public PinpointPluginTestStatement(PinpointPluginTestRunner runner, RunNotifier notifier, PinpointPluginTestContext context, PinpointPluginTestInstance testCase) {
        this.runner = runner;
        this.context = context;
        this.testCase = testCase;
        this.notifier = notifier;
        this.notifier.addListener(result.createListener());
    }
    
    @Override
    public void evaluate() throws Throwable {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command(buildCommand());
        builder.redirectErrorStream(true);
        builder.directory(testCase.getWorkingDirectory());
        
        System.out.println("Working directory: " + SystemProperty.INSTANCE.getProperty("user.dir"));
        System.out.println("Command: " + builder.command());

        Description parentDescription = runner.getDescription();

        final Process process = builder.start();
        
        try {
            Scanner scanner = testCase.startTest(process);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.endsWith("\\r")) {
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
                    } else if ("testStarted".equals(event)) {
                        Description ofTest = findDescription(parentDescription, tokens[2]);
                        notifier.fireTestStarted(ofTest);
                    } else if ("testFinished".equals(event)) {
                        Description ofTest = findDescription(parentDescription, tokens[2]);
                        notifier.fireTestFinished(ofTest);
                    } else if ("testFailure".equals(event)) {
                        List<String> stackTrace = tokens.length > 5 ? Arrays.asList(tokens).subList(5, tokens.length - 1) : Collections.<String>emptyList();
                        Failure failure = toFailure(parentDescription, tokens[2], tokens[3], tokens[4], stackTrace);
                        notifier.fireTestFailure(failure);
                    } else if ("testAssumptionFailure".equals(event)) {
                        List<String> stackTrace = tokens.length > 5 ? Arrays.asList(tokens).subList(5, tokens.length - 1) : Collections.<String>emptyList();
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
            System.err.println("Failed to execute test");
            t.printStackTrace();
            
            throw t;
        } finally {
            try {        
                testCase.endTest(process);
            } finally {

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        process.destroy();
                    }
                    
                }, 10 * 1000);

                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    // ignore
                }
                
                timer.cancel();
            }
        }
    }
    
    private String[] buildCommand() {
        List<String> list = new ArrayList<String>();
        
        list.add(context.getJavaExecutable());
        
        list.add("-cp");
        list.add(getClassPathAsString());
        
        list.add(getAgent());
        
        list.add("-Dpinpoint.agentId=build.test.0");
        list.add("-Dpinpoint.applicationName=test");
        list.add("-D" + PINPOINT_TEST_ID + "=" + testCase.getTestId());

        for (String arg : context.getJvmArguments()) {
            list.add(arg);
        }
        
        if (context.isDebug()) {
            list.addAll(getDebugOptions());
        }
        
        if (context.getConfigFile() != null) {
            list.add("-Dpinpoint.config=" + context.getConfigFile());
        }
        
        for (String arg : testCase.getVmArgs()) {
            list.add(arg);
        }
        
        String mainClass = testCase.getMainClass();
        
        if (mainClass.endsWith(".jar")) {
            list.add("-jar");
        }
        
        list.add(mainClass);
        list.addAll(testCase.getAppArgs());

        return list.toArray(new String[list.size()]);
    }
    
    private List<String> getDebugOptions() {
        return Arrays.asList("-Xdebug", "-agentlib:jdwp=transport=dt_socket,address=1296,server=y,suspend=y");
    }
    
    private String getAgent() {
        return "-javaagent:" + context.getAgentJar() + "=AGENT_TYPE=PLUGIN_TEST";
    }
    
    private String getClassPathAsString() {
        StringBuilder classPath = new StringBuilder();
        boolean first = true;
        
        for (String lib : testCase.getClassPath()) {
            if (first) {
                first = false;
            } else {
                classPath.append(File.pathSeparatorChar);
            }
            
            classPath.append(lib);
        }
        
        return classPath.toString();
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
    
    private PinpointPluginTestException toException(String message, String exceptionClass, List<String> traceInText) {
        StackTraceElement[] stackTrace = new StackTraceElement[traceInText.size()];
        
        for (int i = 0; i < traceInText.size(); i++) {
            String trace = traceInText.get(i);

            if (trace.equals("$CAUSE$")) {
                PinpointPluginTestException cause = toException(traceInText.get(i + 2), traceInText.get(i + 1), traceInText.subList(i + 3, traceInText.size()));
                return new PinpointPluginTestException(exceptionClass + ": " + message, Arrays.copyOf(stackTrace, i), cause);
            }
            
            String[] tokens = trace.split(",");
            
            if (tokens.length != 4) {
                System.out.println("Unexpected trace string: " + trace);
                stackTrace[i] = new StackTraceElement(trace, "", null, -1);
            } else {
                stackTrace[i] = new StackTraceElement(tokens[0], tokens[1], tokens[2], Integer.parseInt(tokens[3]));
            }
            
        }
        
        return new PinpointPluginTestException(exceptionClass + ": " + message, stackTrace);
    }
}
