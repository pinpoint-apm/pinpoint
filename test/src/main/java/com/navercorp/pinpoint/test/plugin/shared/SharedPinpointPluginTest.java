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

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.util.ArrayUtils;
import com.navercorp.pinpoint.test.plugin.ForkedPinpointPluginTestRunner;
import com.navercorp.pinpoint.test.plugin.PluginTestClassLoader;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.test.plugin.PinpointPluginTestConstants.JUNIT_OUTPUT_DELIMITER;

/**
 * @author Taejin Koo
 */
public class SharedPinpointPluginTest {

    public static void main(String[] args) throws Exception {
        final String mavenDependencyResolverClassPaths = System.getProperty(SharedPluginTestConstants.MAVEN_DEPENDENCY_RESOLVER_CLASS_PATHS);
        if (mavenDependencyResolverClassPaths == null) {
            System.out.println("mavenDependencyResolverClassPaths must not be empty");
            return;
        }

        final String testLocation = System.getProperty(SharedPluginTestConstants.TEST_LOCATION);
        if (testLocation == null) {
            System.out.println("testLocation must not be empty");
            return;
        }

        final String testClazzName = System.getProperty(SharedPluginTestConstants.TEST_CLAZZ_NAME);
        if (testClazzName == null) {
            System.out.println("testClazzName must not be empty");
            return;
        }

        if (ArrayUtils.isEmpty(args)) {
            System.out.println("test must not be empty");
            return;
        }

        TestParameterParser parser = new TestParameterParser();
        List<TestParameter> testParameters = parser.parse(args);
        SharedPinpointPluginTest pluginTest = new SharedPinpointPluginTest(testClazzName, testLocation, mavenDependencyResolverClassPaths, testParameters, System.out);
        pluginTest.execute();

    }



    private final String testClazzName;
    private final String testLocation;
    private final String mavenDependencyResolverClassPaths;
    private final List<TestParameter> testParameters;
    private final PrintStream out;

    public SharedPinpointPluginTest(String testClazzName, String testLocation, String mavenDependencyResolverClassPaths, List<TestParameter> testParameters, PrintStream out) {
        this.testClazzName = testClazzName;
        this.testLocation = testLocation;
        this.mavenDependencyResolverClassPaths = mavenDependencyResolverClassPaths;
        this.testParameters = testParameters;
        this.out = out;

    }

    private List<TestInfo> newTestCaseInfo(List<TestParameter> testParameters, File testClazzLocation, ClassLoader mavenDependencyResolverClassLoader) throws Exception {
        List<TestInfo> testInfos = new ArrayList<TestInfo>();
        for (TestParameter testParameter: testParameters) {

            List<File> testDependencyFileList = getTestDependencies(testParameter.getMavenDependencies(), mavenDependencyResolverClassLoader);
            testDependencyFileList.add(testClazzLocation);

            final TestInfo testInfo = new TestInfo(testParameter.getTestId(), testDependencyFileList);
            testInfos.add(testInfo);
        }
        return testInfos;
    }

    private List<File> getTestDependencies(String testMavenDependencies, ClassLoader mavenDependencyResolverClassLoader) throws Exception {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(mavenDependencyResolverClassLoader);
            List<File> fileList = ReflectionDependencyResolver.get(testMavenDependencies);
            return fileList;
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    private void logTestInformation() {
        String testStartClazzName = SharedPinpointPluginTest.class.getSimpleName();

        StringBuilder log = new StringBuilder();
        log.append("[").append(testStartClazzName).append("]");
        log.append(SharedPluginTestConstants.TEST_CLAZZ_NAME).append(":").append(this.testClazzName);
        log.append(", ");
        log.append(SharedPluginTestConstants.MAVEN_DEPENDENCY_RESOLVER_CLASS_PATHS).append(":").append(mavenDependencyResolverClassPaths);

        this.out.println(log.toString());

        for (TestParameter testParameter: testParameters) {
            this.out.println("[" + testClazzName + "] " + testParameter);
        }
    }

    public void execute() throws Exception {
        logTestInformation();
        String[] libs = mavenDependencyResolverClassPaths.split(File.pathSeparator);
        ClassLoader mavenDependencyResolverClassLoader = MavenDependencyResolverClassLoader.getClassLoader(libs);

        File testClazzLocation = new File(testLocation);
        List<TestInfo> testInfos = newTestCaseInfo(testParameters, testClazzLocation, mavenDependencyResolverClassLoader);
        for (TestInfo testInfo : testInfos) {
            execute(testInfo);
        }
    }

    private void execute(final TestInfo testInfo) {
        try {
            ClassLoader testClassLoader = PluginTestClassLoader.getClassLoader(testInfo.getDependencyFileList());

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Class<?> testClazz = Thread.currentThread().getContextClassLoader().loadClass(testClazzName);

                        JUnitCore junit = new JUnitCore();
                        junit.addListener(new PrintListener());

                        Runner runner = new ForkedPinpointPluginTestRunner(testClazz, testInfo.getTestId());
                        junit.run(runner);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
            String threadName = testClazzName + " " + testInfo.getTestId() + " Thread";
            Thread testThread = newThread(runnable, threadName, testClassLoader);
            testThread.start();

            testThread.join(TimeUnit.MINUTES.toMillis(5));

            checkTerminatedState(testThread, testClazzName + " " + testInfo.getTestId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PluginTestVerifierHolder.getInstance().cleanUp(true);
        }
    }

    private void checkTerminatedState(Thread testThread, String testInfo) {
        final Thread.State state = testThread.getState();
        if (state != Thread.State.TERMINATED) {
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
            StringBuilder builder = new StringBuilder();

            builder.append(failure.getTestHeader());
            builder.append(JUNIT_OUTPUT_DELIMITER);

            Throwable t = failure.getException();

            while (true) {
                builder.append(t.getClass().getName());
                builder.append(JUNIT_OUTPUT_DELIMITER);
                builder.append(t.getMessage());
                builder.append(JUNIT_OUTPUT_DELIMITER);

                for (StackTraceElement e : failure.getException().getStackTrace()) {
                    builder.append(e.getClassName());
                    builder.append(',');
                    builder.append(e.getMethodName());
                    builder.append(',');
                    builder.append(e.getFileName());
                    builder.append(',');
                    builder.append(e.getLineNumber());

                    builder.append(JUNIT_OUTPUT_DELIMITER);
                }

                Throwable cause = t.getCause();

                if (cause == null || t == cause) {
                    break;
                }

                t = cause;
                builder.append("$CAUSE$");
                builder.append(JUNIT_OUTPUT_DELIMITER);
            }

            return builder.toString();
        }
    }

}
