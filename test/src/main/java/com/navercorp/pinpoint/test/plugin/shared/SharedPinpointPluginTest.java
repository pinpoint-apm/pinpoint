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
import com.navercorp.pinpoint.test.plugin.ForkedPinpointPluginTestRunner;
import com.navercorp.pinpoint.test.plugin.PluginTestClassLoader;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.test.plugin.PinpointPluginTestConstants.JUNIT_OUTPUT_DELIMITER;

/**
 * @author Taejin Koo
 */
public class SharedPinpointPluginTest {

    public static void main(String[] args) throws Exception {
        String mavenDependencyResolverClassPaths = System.getProperty(SharedPluginTestConstants.MAVEN_DEPENDENCY_RESOLVER_CLASS_PATHS);
        String testLocation = System.getProperty(SharedPluginTestConstants.TEST_LOCATION);
        String testClazzName = System.getProperty(SharedPluginTestConstants.TEST_CLAZZ_NAME);
        if (mavenDependencyResolverClassPaths == null || testLocation == null || testClazzName == null) {
            System.out.println("must not be empty required properties");
            return;
        }
        if (args == null || args.length == 0) {
            System.out.println("test must not be empty");
        }

        logTestInformation(testClazzName, mavenDependencyResolverClassPaths, args);

        ClassLoader mavenDependencyResolverClassLoader = MavenDependencyResolverClassLoader.getClassLoader(mavenDependencyResolverClassPaths.split(File.pathSeparator));

        File testClazzLocation = new File(testLocation);
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }

            String[] testInfo = args[i].split("=");
            if (testInfo == null || testInfo.length != 2) {
                continue;
            }

            String testId = testInfo[0];
            String testMavenDependencies = testInfo[1];

            List<File> testDependencyFileList = getTestDependencies(testMavenDependencies, testClazzLocation, mavenDependencyResolverClassLoader);
            execute(testClazzName, testId, testDependencyFileList);
        }
    }

    private static List<File> getTestDependencies(String testMavenDependencies, File testClazzLocation, ClassLoader mavenDependencyResolverClassLoader) throws Exception {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(mavenDependencyResolverClassLoader);

            List<File> fileList = ReflectionDependencyResolver.get(testMavenDependencies);
            fileList.add(testClazzLocation);
            return fileList;
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    private static void logTestInformation(String testClazzName, String mavenDependencyResolverClassPaths, String[] testInfos) {
        String testStartClazzName = SharedPinpointPluginTest.class.getSimpleName();

        StringBuilder log = new StringBuilder();
        log.append("[").append(testStartClazzName).append("]");
        log.append(SharedPluginTestConstants.TEST_CLAZZ_NAME).append(":").append(testClazzName);
        log.append(", ");
        log.append(SharedPluginTestConstants.MAVEN_DEPENDENCY_RESOLVER_CLASS_PATHS).append(":").append(mavenDependencyResolverClassPaths);

        System.out.println(log.toString());

        for (String testInfo : testInfos) {
            System.out.println("[" + testClazzName + "] " + testInfo);
        }
    }

    private static void execute(final String testClazzName, final String testId, List<File> testDependencyFileList) {
        try {
            ClassLoader testClassLoader = PluginTestClassLoader.getClassLoader(testDependencyFileList);

            final CountDownLatch latch = new CountDownLatch(1);

            Thread testThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Class<?> testClazz = Thread.currentThread().getContextClassLoader().loadClass(testClazzName);

                        JUnitCore junit = new JUnitCore();
                        junit.addListener(new PrintListener());

                        Runner runner = new ForkedPinpointPluginTestRunner(testClazz, testId);
                        junit.run(runner);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
            testThread.setName(testClazzName + " " + testId + " Thread");
            testThread.setContextClassLoader(testClassLoader);
            testThread.setDaemon(true);
            testThread.start();

            latch.await(5, TimeUnit. MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PluginTestVerifierHolder.getInstance().cleanUp(true);
        }
    }

    private static class PrintListener extends RunListener {

        @Override
        public void testRunStarted(Description description) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMITER + "testRunStarted");
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMITER + "testRunFinished");
        }

        @Override
        public void testStarted(Description description) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMITER + "testStarted" + JUNIT_OUTPUT_DELIMITER + description.getDisplayName());
        }

        @Override
        public void testFinished(Description description) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMITER + "testFinished" + JUNIT_OUTPUT_DELIMITER + description.getDisplayName());
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMITER + "testFailure" + JUNIT_OUTPUT_DELIMITER + failureToString(failure));
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            System.out.println(JUNIT_OUTPUT_DELIMITER + "testAssumptionFailure" + JUNIT_OUTPUT_DELIMITER + failureToString(failure));
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMITER + "testIgnored" + JUNIT_OUTPUT_DELIMITER + description.getDisplayName());
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
