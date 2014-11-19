package com.nhn.pinpoint.profiler.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.nhn.pinpoint.common.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 


public class ForkRunner extends BlockJUnit4ClassRunner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String agentJar;
    private final String configPath;

    public ForkRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        final String property = System.getProperty("user.dir");
        logger.debug("user.dir:{}", property);

        ProjectPathResolver projectPathResolver = new ProjectPathResolver();
        ProjectPathResolver.ProjectPath projectPath = projectPathResolver.resolvePathFromTestClass(testClass);
        logger.debug("path:{}", projectPath);

        PinpointAgent path = testClass.getAnnotation(PinpointAgent.class);
        if (path != null && path.value() != null) {
            agentJar = projectPath.getPinpointAgentPath() + "/" + path.value();
        } else {
            agentJar = projectPath.getPinpointAgentPath() + "/" + "pinpoint-bootstrap-" + Version.VERSION + ".jar";
        }
        logger.debug("agentJar:{}", agentJar);

        PinpointConfig config = testClass.getAnnotation(PinpointConfig.class);

        if (config != null && config.value() != null) {
            configPath = projectPath.getTestClassPath() + "/" + config.value();
        } else {
            configPath = projectPath.getPinpointAgentPath() + "/" + "pinpoint.config";
        }
        logger.debug("configPath:{}", configPath);

    }




    @Override
    protected Statement classBlock(RunNotifier notifier) {
        if (ForkedJUnit.isForked()) {
            return super.classBlock(notifier);
        }

        return new ForkedTest(notifier);
    }

    private class ForkedTest extends Statement {
        private final RunNotifier notifier;
        private final Result result = new Result();

        public ForkedTest(RunNotifier notifier) {
            this.notifier = notifier;
            this.notifier.addListener(result.createListener());
        }

        @Override
        public void evaluate() throws Throwable {
            ProcessBuilder builder = new ProcessBuilder();

            builder.command(buildCommand());
            builder.redirectErrorStream(true);

            Process process;
            try {
                process = builder.start();
            } catch (IOException e) {
                return;
            }

            Scanner out = new Scanner(process.getInputStream());

            try {
                while (out.hasNextLine()) {
                    String line = out.nextLine();

                    if (line.startsWith(ForkedJUnit.JUNIT_OUTPUT_DELIMETER)) {
                        String[] tokens = line.split(ForkedJUnit.JUNIT_OUTPUT_DELIMETER_REGEXP);
                        String event = tokens[1];

                        if ("testRunStarted".equals(event)) {
                            notifier.fireTestRunStarted(getDescription());
                        } else if ("testRunFinished".equals(event)) {
                            notifier.fireTestRunFinished(result);
                        } else if ("testStarted".equals(event)) {
                            Description ofTest = findDescription(getDescription(), tokens[2]);
                            notifier.fireTestStarted(ofTest);
                        } else if ("testFinished".equals(event)) {
                            Description ofTest = findDescription(getDescription(), tokens[2]);
                            notifier.fireTestFinished(ofTest);
                        } else if ("testFailure".equals(event)) {
                            Failure failure = toFailure(tokens[2], tokens[3], tokens[4], Arrays.asList(tokens).subList(5, tokens.length - 1));
                            notifier.fireTestFailure(failure);
                        } else if ("testAssumptionFailure".equals(event)) {
                            Failure failure = toFailure(tokens[2], tokens[3], tokens[4], Arrays.asList(tokens).subList(5, tokens.length - 1));
                            notifier.fireTestAssumptionFailed(failure);
                        } else if ("testIgnored".equals(event)) {
                            Description ofTest = findDescription(getDescription(), tokens[2]);
                            notifier.fireTestIgnored(ofTest);
                        }
                    } else {
                        System.out.println(line);
                    }
                }
            } finally {
                out.close();
            }

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                process.destroy();
            }
        }

        private String[] buildCommand() {
            List<String> list = new ArrayList<String>();

            list.add(getJavaExecutable());
            list.add("-cp");
            list.add(getClassPath());
            list.add(getAgent());
            list.add("-Dpinpoint.agentId=build.test.0");
            list.add("-Dpinpoint.applicationName=test");

            if (configPath != null) {
                list.add("-Dpinpoint.config=" + configPath);
            }

            list.add(ForkedJUnit.class.getName());
            list.add(getTestClass().getName());
            logger.debug("command:{}", list);
            return list.toArray(new String[list.size()]);
        }

        private String getAgent() {
            return "-javaagent:" + agentJar;
        }

        private String getJavaExecutable() {
            StringBuilder builder = new StringBuilder();

            builder.append(System.getProperty("java.home"));
            builder.append(File.separatorChar);
            builder.append("bin");
            builder.append(File.separatorChar);
            builder.append("java");

            if (System.getProperty("os.name").contains("indows")) {
                builder.append(".exe");
            }

            return builder.toString();
        }

        private String getClassPath() {
            URLClassLoader cl = (URLClassLoader) ClassLoader.getSystemClassLoader();
            StringBuilder classPath = new StringBuilder();

            for (URL url : cl.getURLs()) {
                classPath.append(url.getPath());
                classPath.append(File.pathSeparatorChar);
            }

            return classPath.toString();
        }

        private Description findDescription(Description description, String displayName) {
            if (displayName.equals(description.getDisplayName())) {
                return description;
            }

            for (Description desc : description.getChildren()) {
                Description found = findDescription(desc, displayName);

                if (found != null) {
                    return found;
                }
            }

            return null;
        }

        private Failure toFailure(String displayName, String exceptionClass, String message, List<String> trace) {
            Description desc = findDescription(getDescription(), displayName);
            Exception exception = toException(message, exceptionClass, trace);
            Failure failure = new Failure(desc, exception);

            return failure;
        }

        private ChildProcessException toException(String message, String exceptionClass, List<String> traceInText) {
            StackTraceElement[] stackTrace = new StackTraceElement[traceInText.size()];

            for (int i = 0; i < traceInText.size(); i++) {
                String trace = traceInText.get(i);
                System.out.println(trace);
                String[] tokens = trace.split(",");

                stackTrace[i] = new StackTraceElement(tokens[0], tokens[1], tokens[2], Integer.valueOf(tokens[3]));
            }

            return new ChildProcessException(exceptionClass + ": " + message, stackTrace);
        }
    }

    @SuppressWarnings("serial")
    private static class ChildProcessException extends Exception {
        public ChildProcessException(String message, StackTraceElement[] stackTrace) {
            super(message);
            setStackTrace(stackTrace);
        }
    }
}
