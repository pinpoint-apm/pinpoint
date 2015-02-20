/*
 * Copyright 2014 NAVER Corp.
 *
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.navercorp.pinpoint.common.util.SystemProperty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.exception.PinpointException;

public class PinpointPluginTestSuite extends Suite {
    private static final String DEFAULT_ENCODING = "UTF-8";
    
    private static final String[] REQUIRED_CLASS_PATHS = new String[] {
        "junit", // JUNit
        "hamcrest-core", // for JUnit
        "pinpoint-test", // pinpoint-test-{VERSION}.jar
        "/test/target/classes" // pinpoint-test build output directory
    };

    private final List<Runner> runners = new ArrayList<Runner>();
    private final List<String> systemLibs;
    
    private final String agentJar;
    private final String configFile;
    private final boolean testOnSystemClassLoader;
    private final boolean testOnChildClassLoader;
    private final String[] repositories;
    private final String[] dependencies;
    private final String libraryPath;
    private final String[] librarySubDirs;
    private final String[] jvmArguments;
    private final String javaHomeEnvName;

    private final SystemProperty simpleProperty = SystemProperty.INSTANCE;

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    public PinpointPluginTestSuite(Class<?> testClass) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
        super(testClass, Collections.<Runner> emptyList());

        PinpointAgent agent = testClass.getAnnotation(PinpointAgent.class);
        this.agentJar = resolveAgentPath(agent);

        PinpointConfig config = testClass.getAnnotation(PinpointConfig.class);
        this.configFile = config == null ? null : config.value();

        OnClassLoader onClassLoader = testClass.getAnnotation(OnClassLoader.class);
        this.testOnChildClassLoader = onClassLoader == null ? true : onClassLoader.child();
        this.testOnSystemClassLoader = onClassLoader == null ? true : onClassLoader.system();

        Dependency deps = testClass.getAnnotation(Dependency.class);
        this.dependencies = deps == null ? null : deps.value();
        
        TestRoot lib = testClass.getAnnotation(TestRoot.class);
        
        if (lib == null) {
            this.libraryPath = null;
            this.librarySubDirs = null;
        } else {
            String path = lib.value();
            
            if (path.isEmpty()) {
                path = lib.path();
            }
            
            this.libraryPath = path;
            this.librarySubDirs = lib.libraryDir();
        }
        
        if (deps != null && lib != null) {
            throw new IllegalArgumentException("@Dependency and @TestRoot can not annotate a class at the same time");
        }

        Repository repos = testClass.getAnnotation(Repository.class);
        this.repositories = repos == null ? new String[0] : repos.value();

        JvmArgument jvmArgument = testClass.getAnnotation(JvmArgument.class);
        this.jvmArguments = jvmArgument == null ? new String[0] : jvmArgument.value();

        JavaVersion javaVersion = testClass.getAnnotation(JavaVersion.class);
        this.javaHomeEnvName = javaVersion == null ? null : "JAVA_" + javaVersion.value() + "_HOME";

        this.systemLibs = resolveSystemLibraries();
        
        String testClassLocation = resolveTestClassLocation(testClass);
        
        if (dependencies != null) {
            addRunnersWithDependencies(testClass, testClassLocation);
        } else if (libraryPath != null) {
            addRunnersWithLibraryPath(testClass, testClassLocation);
        } else {
            addRunner(testClass, "", Arrays.asList(testClassLocation));
        }
    }
    
    private void addRunner(Class<?> testClass, String testId, List<String> libraries) throws InitializationError {
        if (testOnChildClassLoader) {
            PinpointPluginTestRunner runner = new PinpointPluginTestRunner(testClass, testId, libraries, true);
            runners.add(runner);
        }
        
        if (testOnSystemClassLoader) {
            PinpointPluginTestRunner runner = new PinpointPluginTestRunner(testClass, testId, libraries, false);
            runners.add(runner);
        }
    }

    private void addRunnersWithLibraryPath(Class<?> testClass, String testClassLocation) throws InitializationError {
        File file = new File(libraryPath);
        
        for (File child : file.listFiles()) {
            if (!child.isDirectory()) {
                continue;
            }
            
            List<String> libraries = new ArrayList<String>();
            
            if (librarySubDirs.length == 0) {
                addJars(child, libraries);
                libraries.add(child.getAbsolutePath());
            } else {
                for (String subDir : librarySubDirs) {
                    File libDir = new File(child, subDir);
                    addJars(libDir, libraries);
                    libraries.add(libDir.getAbsolutePath());
                }
            }
            
            libraries.add(testClassLocation);
            
            addRunner(testClass, child.getName(), libraries);
        }
    }

    private void addJars(File libDir, List<String> libraries) {
        for (File f : libDir.listFiles()) {
            if (f.getName().endsWith(".jar")) {
                libraries.add(f.getAbsolutePath());
            }
        }
    }
    
    private void addRunnersWithDependencies(Class<?> testClass, String testClassLocation) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
        DependencyResolver resolver = DependencyResolver.get(repositories);
        Map<String, List<Artifact>> dependencyCases = resolver.resolveDependencySets(dependencies);
        
        for (Map.Entry<String, List<Artifact>> dependencyCase : dependencyCases.entrySet()) {
            List<String> libs = new ArrayList<String>();
            libs.add(testClassLocation);

            for (File lib : resolver.resolveArtifactsAndDependencies(dependencyCase.getValue())) {
                libs.add(lib.getAbsolutePath());
            }

            addRunner(testClass, dependencyCase.getKey(), libs);
        }
    }

    private String resolveTestClassLocation(Class<?> testClass) {
        CodeSource codeSource = testClass.getProtectionDomain().getCodeSource();
        URL testClassLocation = codeSource.getLocation();

        return toPathString(testClassLocation);
    }

    private List<String> resolveSystemLibraries() {
        List<String> result = new ArrayList<String>();
        URLClassLoader cl = (URLClassLoader) ClassLoader.getSystemClassLoader();

        outer: for (URL url : cl.getURLs()) {
            for (String required : REQUIRED_CLASS_PATHS) {
                if (url.getFile().contains(required)) {
                    result.add(toPathString(url));

                    continue outer;
                }
            }
        }

        return result;
    }

    private String toPathString(URL url) {
        try {
            return new File(url.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException("???", e);
        }
    }

    private String resolveAgentPath(PinpointAgent agent) {
        String path = agent == null ? "target/pinpoint-agent-" + Version.VERSION : agent.value();
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
    protected Statement withBeforeClasses(Statement statement) {
        List<FrameworkMethod> befores= getTestClass().getAnnotatedMethods(BeforePinpointPluginTest.class);
        return befores.isEmpty() ? statement : new RunBefores(statement, befores, null);
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        List<FrameworkMethod> afters= getTestClass().getAnnotatedMethods(AfterPinpointPluginTest.class);
        return afters.isEmpty() ? statement : new RunAfters(statement, afters, null);
    }

    
    private class PinpointPluginTestRunner extends BlockJUnit4ClassRunner {
        private final String testId;
        private final String testName;
        private final List<String> dependencyLibs;
        private final boolean testOnChildClassLoader;

        PinpointPluginTestRunner(Class<?> testClass, String id, List<String> dependencyLibs, boolean testOnChildClassLoader) throws InitializationError {
            super(testClass);
            
            this.testId = id;
            this.testName = PluginTestProperty.makeTestName(testId, testOnChildClassLoader);
            this.dependencyLibs = dependencyLibs;
            this.testOnChildClassLoader = testOnChildClassLoader;
        }

        @Override
        protected String getName() {
            return String.format("[%s]", testName);
        }

        @Override
        protected String testName(final FrameworkMethod method) {
            return String.format("%s[%s]", method.getName(), testName);
        }

        @Override
        protected Statement classBlock(RunNotifier notifier) {
            if (ForkedPinpointPluginTest.isForked()) {
                return super.classBlock(notifier);
            }
            
            return new PluginTest(notifier);
        }
        
        private class PluginTest extends Statement {
            private final RunNotifier notifier;
            private final Result result = new Result();
            
            public PluginTest(RunNotifier notifier) {
                this.notifier = notifier;
                this.notifier.addListener(result.createListener());
            }
            
            @Override
            public void evaluate() throws Throwable {
                ProcessBuilder builder = new ProcessBuilder();

                builder.command(buildCommand(systemLibs, dependencyLibs));
                builder.redirectErrorStream(true);

                System.out.println("Working directory: " + simpleProperty.getProperty("user.dir"));
                System.out.println("Command: " + builder.command());

                Process process = builder.start();

                final InputStream inputStream = process.getInputStream();
                final Scanner out = new Scanner(inputStream, DEFAULT_ENCODING);
                try {
                    while (out.hasNextLine()) {
                        String line = out.nextLine();

                        if (line.startsWith(ForkedPinpointPluginTest.JUNIT_OUTPUT_DELIMETER)) {
                            String[] tokens = line.split(ForkedPinpointPluginTest.JUNIT_OUTPUT_DELIMETER_REGEXP);
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
                    close(inputStream);
                }

                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    process.destroy();
                }
            }
            
            private String[] buildCommand(List<String> defaultLibs, List<String> dependencyLibs) {
                List<String> list = new ArrayList<String>();
                
                list.add(getJavaExecutable());
                list.add("-cp");
                list.add(getClassPath(defaultLibs, dependencyLibs));
                list.add(getAgent());
                
                list.add("-Dpinpoint.agentId=build.test.0");
                list.add("-Dpinpoint.applicationName=test");
                list.add("-Dfile.encoding=" + DEFAULT_ENCODING);
                list.add("-D" + PluginTestProperty.PINPOINT_TEST_ID + "=" + testId);
                
                String testDirectory = libraryPath + "/" + testId;

                if (libraryPath != null) {
                    list.add("-D" + PluginTestProperty.PINPOINT_TEST_DIRECTORY + "=" + testDirectory);
                }
                
                for (String arg : jvmArguments) {
                    String replaced = arg.replace("${" + PluginTestProperty.PINPOINT_TEST_DIRECTORY + "}", testDirectory);
                    list.add(replaced);
                }
                
                if (isDebugMode()) {
                    list.addAll(getDebugOptions());
                }
                
                if (configFile != null) {
                    list.add("-Dpinpoint.config=" + resolveConfigFileLocation());
                }
                
                list.add(ForkedPinpointPluginTest.class.getName());
                list.add(getTestClass().getName());

                if (testOnChildClassLoader) {
                    list.add(getChildClassPath(dependencyLibs));
                }
                
                return list.toArray(new String[list.size()]);
            }
            
            private String resolveConfigFileLocation() {
                URL url = getClass().getResource(configFile.startsWith("/") ? configFile : "/" + configFile);
                
                if (url != null) {
                    return toPathString(url);
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
                return "-javaagent:" + agentJar + "=bootClass=com.navercorp.pinpoint.test.PluginTestAgent";
            }
            
            private String getJavaExecutable() {
                StringBuilder builder = new StringBuilder();
                
                String javaHome;
                if (javaHomeEnvName == null) {
                    javaHome = simpleProperty.getProperty("java.home");
                } else {
                    javaHome = simpleProperty.getEnv(javaHomeEnvName);
                }
                
                builder.append(javaHome);
                builder.append(File.separatorChar);
                builder.append("bin");
                builder.append(File.separatorChar);
                builder.append("java");
                
                if (simpleProperty.getProperty("os.name").contains("indows")) {
                    builder.append(".exe");
                }
                
                return builder.toString();
            }
            
            private String getClassPath(List<String> defaultLibs, List<String> dependencyLibs) {
                StringBuilder classPath = new StringBuilder();
                
                for (String lib : defaultLibs) {
                    classPath.append(lib);
                    classPath.append(File.pathSeparatorChar);
                }
                
                if (!testOnChildClassLoader) {
                    for (String lib : dependencyLibs) {
                        classPath.append(lib);
                        classPath.append(File.pathSeparatorChar);
                    }
                }
                
                return classPath.toString();
            }
            
            private String getChildClassPath(List<String> dependencyLibs) {
                StringBuilder classPath = new StringBuilder();
                classPath.append(ForkedPinpointPluginTest.CHILD_CLASS_PATH_PREFIX);
                
                for (String lib : dependencyLibs) {
                    classPath.append(lib);
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
                    String[] tokens = trace.split(",");
                    
                    stackTrace[i] = new StackTraceElement(tokens[0], tokens[1], tokens[2], Integer.parseInt(tokens[3]));
                }
                
                return new ChildProcessException(exceptionClass + ": " + message, stackTrace);
            }
        }
    }



    private void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignore) {
            // skip
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
