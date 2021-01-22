/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import com.navercorp.pinpoint.test.plugin.ExceptionWriter;
import com.navercorp.pinpoint.test.plugin.PluginClassLoading;
import com.navercorp.pinpoint.test.plugin.ReflectPluginTestVerifier;
import com.navercorp.pinpoint.test.plugin.util.ArrayUtils;
import com.navercorp.pinpoint.test.plugin.ForkedPinpointPluginTestRunner;
import com.navercorp.pinpoint.test.plugin.util.ChildFirstClassLoader;
import com.navercorp.pinpoint.test.plugin.util.ProfilerClass;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import com.navercorp.pinpoint.test.plugin.util.ThreadContextCallable;
import com.navercorp.pinpoint.test.plugin.util.URLUtils;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.InitializationError;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.JUNIT_OUTPUT_DELIMITER;

/**
 * @author Taejin Koo
 */
public class SharedPinpointPluginTest {
    private static final TaggedLogger logger = TestLogger.getLogger();

    public static void main(String[] args) throws Exception {
        final String mavenDependencyResolverClassPaths = System.getProperty(SharedPluginTestConstants.MAVEN_DEPENDENCY_RESOLVER_CLASS_PATHS);
        if (mavenDependencyResolverClassPaths == null) {
            logger.error("mavenDependencyResolverClassPaths must not be empty");
            return;
        }

        final String testLocation = System.getProperty(SharedPluginTestConstants.TEST_LOCATION);
        if (testLocation == null) {
            logger.error("testLocation must not be empty");
            return;
        }
        logger.debug("-D{}={}", SharedPluginTestConstants.TEST_LOCATION, testLocation);

        final String testClazzName = System.getProperty(SharedPluginTestConstants.TEST_CLAZZ_NAME);
        if (testClazzName == null) {
            logger.error("testClazzName must not be empty");
            return;
        }
        logger.debug("-D{}={}", SharedPluginTestConstants.TEST_CLAZZ_NAME, testClazzName);

        String loggerEnable = System.getProperty(SharedPluginTestConstants.TEST_LOGGER);
        if (loggerEnable == null) {
            logger.debug("-D{} is not set", SharedPluginTestConstants.TEST_LOGGER);
            loggerEnable = Boolean.TRUE.toString();
        }
        final boolean testLogger = Boolean.parseBoolean(loggerEnable);
        logger.debug("-D{}={}", SharedPluginTestConstants.TEST_LOGGER, testLogger);

        if (ArrayUtils.isEmpty(args)) {
            logger.error("test must not be empty");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("main args:{}", Arrays.toString(args));
        }

        String[] mavenDependencyResolverClassPathArray = mavenDependencyResolverClassPaths.split(File.pathSeparator);
        TestParameterParser parser = new TestParameterParser();
        List<TestParameter> testParameters = parser.parse(args);
        SharedPinpointPluginTest pluginTest = new SharedPinpointPluginTest(testClazzName, testLocation, testLogger,
                mavenDependencyResolverClassPathArray, testParameters, System.out);
        pluginTest.execute();

    }

    private final String testClazzName;
    private final String testLocation;
    private final boolean testLogger;
    private final String[] mavenDependencyResolverClassPaths;
    private final List<TestParameter> testParameters;
    private final PrintStream out;

    public SharedPinpointPluginTest(String testClazzName, String testLocation, boolean testLogger,
                                    String[] mavenDependencyResolverClassPaths, List<TestParameter> testParameters, PrintStream out) {
        this.testClazzName = testClazzName;
        this.testLocation = testLocation;
        this.testLogger = testLogger;
        this.mavenDependencyResolverClassPaths = mavenDependencyResolverClassPaths;
        this.testParameters = testParameters;
        this.out = out;

    }

    private List<TestInfo> newTestCaseInfo(List<TestParameter> testParameters, File testClazzLocation, ClassLoader dependencyClassLoader) throws Exception {
        ReflectionDependencyResolver dependencyResolver = new ReflectionDependencyResolver(dependencyClassLoader);
        List<File> loggerDependencies = getLoggerDependencies(dependencyResolver, dependencyClassLoader);
        logger.debug("loggerDependency:{}", loggerDependencies);

        List<TestInfo> testInfos = new ArrayList<TestInfo>();
        for (TestParameter testParameter : testParameters) {
            final List<File> testDependency = new ArrayList<>();
            testDependency.add(testClazzLocation);

            testDependency.addAll(loggerDependencies);

            List<File> testParameterDependency = getTestParameterDependency(dependencyClassLoader, dependencyResolver, testParameter);
            testDependency.addAll(testParameterDependency);

            final TestInfo testInfo = new TestInfo(testParameter.getTestId(), testDependency);
            testInfos.add(testInfo);
        }
        return testInfos;
    }

    private List<File> getTestParameterDependency(ClassLoader mavenDependencyResolverClassLoader,
                                                  ReflectionDependencyResolver dependencyResolver,
                                                  TestParameter testParameter) throws Exception {

        final List<String> mavenDependencies = testParameter.getMavenDependencies();
        List<File> testDependencyFileList = lookup(dependencyResolver, mavenDependencies, mavenDependencyResolverClassLoader);
        if (logger.isDebugEnabled()) {
            logger.debug("@Dependency {}", mavenDependencies);
            for (File file : testDependencyFileList) {
                logger.debug("-> {}", file);
            }
        }
        return testDependencyFileList;
    }

    private List<File> getLoggerDependencies(ReflectionDependencyResolver dependencyResolver, ClassLoader mavenDependencyResolverClassLoader) throws Exception {
        if (!testLogger) {
            return Collections.emptyList();
        }
        List<String> dependencyLib = Arrays.asList(PluginClassLoading.LOGGER_DEPENDENCY);
        List<File> libFiles = lookup(dependencyResolver, dependencyLib, mavenDependencyResolverClassLoader);
        if (logger.isDebugEnabled()) {
            logger.debug("LoggerDependency {}", dependencyLib);
            for (File libFile : libFiles) {
                logger.debug("-> {}", libFile);
            }
        }
        return libFiles;
    }

    private List<File> lookup(final ReflectionDependencyResolver dependencyResolver, final List<String> dependencyLib, ClassLoader cl) throws Exception {
        Callable<List<File>> callable = new ThreadContextCallable<>(new Callable<List<File>>() {
            @Override
            public List<File> call() throws Exception {
                return dependencyResolver.lookup(dependencyLib);
            }
        }, cl);
        return callable.call();
    }

    private void logTestInformation() {
        logger.info("[{}] {}", getClass().getSimpleName(), testClazzName);

        if (logger.isDebugEnabled()) {
            for (String mavenDependencyResolverClassPath : mavenDependencyResolverClassPaths) {
                logger.debug("{}: {}", SharedPluginTestConstants.MAVEN_DEPENDENCY_RESOLVER_CLASS_PATHS, mavenDependencyResolverClassPath);
            }
            for (TestParameter testParameter : testParameters) {
                logger.debug("{} {}", testClazzName, testParameter);
            }
        }
    }

    public void execute() throws Exception {
        logTestInformation();
        ClassLoader mavenDependencyResolverClassLoader = new ChildFirstClassLoader(URLUtils.fileToUrls(mavenDependencyResolverClassPaths));
        File testClazzLocation = new File(testLocation);
        List<TestInfo> testInfos = newTestCaseInfo(testParameters, testClazzLocation, mavenDependencyResolverClassLoader);
        for (TestInfo testInfo : testInfos) {
            execute(testInfo);
        }
    }

    private void execute(final TestInfo testInfo) {
        try {
            List<File> dependencyFileList = testInfo.getDependencyFileList();
            if (logger.isDebugEnabled()) {
                for (File dependency : dependencyFileList) {
                    logger.debug("testcase cl lib :{}", dependency);
                }
            }
            URL[] urls = URLUtils.fileToUrls(dependencyFileList);
            final ClassLoader testClassLoader = new ChildFirstClassLoader(urls, ProfilerClass.PINPOINT_PROFILER_CLASS);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    final Class<?> testClazz = loadClass();
                    logger.debug("testClazz:{} cl:{}", testClazz.getName(), testClazz.getClassLoader());
                    try {
                        JUnitCore junit = new JUnitCore();
                        junit.addListener(new PrintListener());

                        Runner runner = new ForkedPinpointPluginTestRunner(testClazz, testInfo.getTestId());
                        junit.run(runner);
                    } catch (InitializationError error) {
                        logger.error(error, "test run fail testClazz:{} testId:{}", testClazzName, testInfo.getTestId());
                        List<Throwable> causes = error.getCauses();
                        for (Throwable cause : causes) {
                            logger.error(cause, "junit error Caused By:{}", cause.getMessage());
                        }
                    }
                }

                private Class<?> loadClass() {
                    try {
                        return testClassLoader.loadClass(testClazzName);
                    } catch (ClassNotFoundException e) {
                        logger.error(e, "testClazz:{} not found", testClazzName, testInfo.getTestId());
                        throw new RuntimeException(e);
                    }
                }
            };
            String threadName = testClazzName + " " + testInfo.getTestId() + " Thread";
            Thread testThread = newThread(runnable, threadName, testClassLoader);
            testThread.start();

            testThread.join(TimeUnit.MINUTES.toMillis(5));

            checkTerminatedState(testThread, testClazzName + " " + testInfo.getTestId());
        } catch (Exception e) {
            logger.error(e, "{}:{} execute failed:{}", testClazzName, testInfo.getTestId(), e.getMessage());
        } finally {
            ReflectPluginTestVerifier.getInstance().cleanUp(true);
        }
    }

    private void checkTerminatedState(Thread testThread, String testInfo) {
        if (testThread.isAlive()) {
            throw new IllegalStateException(testInfo + " not finished");
        }
    }

    private Thread newThread(Runnable runnable, String threadName, ClassLoader testClassLoader) {
        Thread testThread = new Thread(runnable);
        testThread.setName(threadName);
        testThread.setContextClassLoader(testClassLoader);
        testThread.setDaemon(true);
        return testThread;
    }

    private class PrintListener extends RunListener {
        private final ExceptionWriter writer = new ExceptionWriter();

        @Override
        public void testRunStarted(Description description) throws Exception {
            out.println(JUNIT_OUTPUT_DELIMITER + "testRunStarted");
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            out.println(JUNIT_OUTPUT_DELIMITER + "testRunFinished");
        }

        @Override
        public void testStarted(Description description) throws Exception {
            out.println(JUNIT_OUTPUT_DELIMITER + "testStarted" + JUNIT_OUTPUT_DELIMITER + description.getDisplayName());
        }

        @Override
        public void testFinished(Description description) throws Exception {
            out.println(JUNIT_OUTPUT_DELIMITER + "testFinished" + JUNIT_OUTPUT_DELIMITER + description.getDisplayName());
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            out.println(JUNIT_OUTPUT_DELIMITER + "testFailure" + JUNIT_OUTPUT_DELIMITER + failureToString(failure));
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            out.println(JUNIT_OUTPUT_DELIMITER + "testAssumptionFailure" + JUNIT_OUTPUT_DELIMITER + failureToString(failure));
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            out.println(JUNIT_OUTPUT_DELIMITER + "testIgnored" + JUNIT_OUTPUT_DELIMITER + description.getDisplayName());
        }

        private String failureToString(Failure failure) {
            return writer.write(failure.getTestHeader(), failure.getException());
        }

    }

}
