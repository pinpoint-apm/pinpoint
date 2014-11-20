package com.nhn.pinpoint.test.fork;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.exception.PinpointException;

public class ForkRunner extends BlockJUnit4ClassRunner {
    private static final String[] REQUIRED_CLASS_PATHS = new String[] {
        "junit",
        "pinpoint-test",
        "pinpoint/test"
    };
    
    private final String agentJar;
    private final String configFile;
    private final boolean testOnChildClassLoader;
    private final String[] excludedLibraries;
    private final String[] includedLibraries;

    public ForkRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        
        PinpointAgent agent = testClass.getAnnotation(PinpointAgent.class);
        this.agentJar = resolveAgentPath(agent); 

        
        PinpointConfig config = testClass.getAnnotation(PinpointConfig.class);
        this.configFile = config == null ? null : config.value();
        
        testOnChildClassLoader = testClass.isAnnotationPresent(OnChildClassLoader.class);
        
        ExcludedLibraries exclude = testClass.getAnnotation(ExcludedLibraries.class);
        excludedLibraries = exclude == null ? new String[0] : exclude.value();
        
        WithLibraries include = testClass.getAnnotation(WithLibraries.class);
        includedLibraries = include == null ? new String[0] : include.value();
    }
    
    private String resolveAgentPath(PinpointAgent agent) {
        String path = agent == null ? "build/pinpoint-agent" : agent.value();
        String version = agent == null ? Version.VERSION : agent.version(); 
        String relativePath = path + (!path.endsWith("/") ? "/" : "") + "pinpoint-bootstrap-" + version + ".jar";

        File parent = new File(".").getAbsoluteFile();
        
        while (true) {
            File candidate = new File(parent, relativePath);
            if (candidate.exists()) {
                return candidate.getAbsolutePath();
            }
            
            parent = parent.getParentFile();
            
            if (parent == null) {
                throw new IllegalArgumentException("Cannot find agent path: " + relativePath);
            }
        }
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
            
            System.out.println("Working directory: " + System.getProperty("user.dir"));
            System.out.println("Command: " + builder.command());

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

        private String[] buildCommand() throws URISyntaxException {
            List<String> list = new ArrayList<String>();

            list.add(getJavaExecutable());
            list.add("-cp");
            list.add(getClassPath());
            list.add(getAgent());
            
            list.add("-Dpinpoint.agentId=build.test.0");
            list.add("-Dpinpoint.applicationName=test");
            
            if (isDebugMode()) {
                list.addAll(getDebugOptions());
            }

            if (configFile != null) {
                list.add("-Dpinpoint.config=" + resolveConfigFileLocation());
            }

            list.add(ForkedJUnit.class.getName());
            
            if (testOnChildClassLoader) {
                list.add(getChildClassPath());
            }
            
            list.add(getTestClass().getName());

            return list.toArray(new String[list.size()]);
        }
        
        private String resolveConfigFileLocation() {
            URL url = getClass().getResource(configFile.startsWith("/") ? configFile : "/" + configFile);
            
            if (url != null) {
                try {
                    return new File(url.toURI()).getAbsolutePath();
                } catch (URISyntaxException e) {
                    throw new PinpointException("Cannot find pinpoint configuration file: " + configFile, e);
                }
            }
            
            File config = new File(configFile);
            if (config.exists()) {
                return config.getAbsolutePath();
            }
            
            throw new PinpointException("Cannot find pinpoint configuration file: " + configFile);
        }
        
        private boolean isDebugMode() {
            return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp");
        }
        
        private List<String> getDebugOptions() {
            return Arrays.asList("-Xdebug", "-agentlib:jdwp=transport=dt_socket,address=1296,server=y,suspend=y");
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
        
        private String getClassPath() throws URISyntaxException {
            StringBuilder classPath = new StringBuilder();

            if (testOnChildClassLoader) {
                URLClassLoader cl = (URLClassLoader) ClassLoader.getSystemClassLoader();

                outer:
                for (URL url : cl.getURLs()) {
                    for (String required : REQUIRED_CLASS_PATHS) {
                        if (url.getFile().contains(required)) {
                            classPath.append(new File(url.toURI()).getAbsolutePath());
                            classPath.append(File.pathSeparatorChar);
                            continue outer;
                        }
                    }
                }
            } else {
                appendClassPath(classPath);
            }

            return classPath.toString();
        }
        
        private void appendClassPath(StringBuilder classPath) throws URISyntaxException {
            URLClassLoader cl = (URLClassLoader) ClassLoader.getSystemClassLoader();
            
            outer:
            for (URL url : cl.getURLs()) {
                String urlAsString = url.toString();

                for (String exclude : excludedLibraries) {
                    if (urlAsString.contains(exclude)) {
                        continue outer;
                    }
                }
                
                classPath.append(new File(url.toURI()).getAbsolutePath());
                classPath.append(File.pathSeparatorChar);
            }
            
            for (String include : includedLibraries) {
                classPath.append(include);
                classPath.append(File.pathSeparatorChar);
            }
            
        }

        private String getChildClassPath() throws URISyntaxException {
            StringBuilder classPath = new StringBuilder();
            
            classPath.append(ForkedJUnit.CHILD_CLASS_PATH_PREFIX);
            appendClassPath(classPath);
            
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
