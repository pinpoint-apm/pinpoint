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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.InitializationError;

import static com.navercorp.pinpoint.test.plugin.PinpointPluginTestConstants.*;

public class ForkedPinpointPluginTest {
    private static boolean forked = false;

    private static PluginTestLogger logger = PluginTestLogger.getLogger(ForkedPinpointPluginTest.class.getName());
    
    public static boolean isForked() {
        return forked;
    }

    

    public static void main(String[] args) throws Exception {
        forked = true;

        final String testClassName = args[0];
        final String agentType = getAgentType(args);

        final ClassLoader classLoader = getClassLoader(agentType);
        
        final String testId = System.getProperty(PINPOINT_TEST_ID, "");
        if (logger.isDebugEnabled()){
            logger.debug("testId:" + testId);
        }

        final Callable<Result> testCaseCallable = new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                Class<?> testClass = classLoader.loadClass(testClassName);
                return runTests(testClass, testId);
            }
        };
        Result result = null;
        try {
            result = executeTestCase(testCaseCallable, classLoader);
        } catch (Throwable e) {
            logger.info("testcase run error:" + e.getMessage());
            System.exit(-1);
        }

        System.exit(getFailureCount(result));
    }


    private static String getAgentType(String[] args) {
        if (args == null) {
            return "";
        }
        if (args.length >= 2) {
            return args[1];
        }
        return "";
    }


    private static int getFailureCount(Result result) {
        if (result == null) {
            return -1;
        }
        return result.getFailureCount();
    }

    private static ClassLoader getClassLoader(String agentType) throws MalformedURLException {
        if (agentType.startsWith(CHILD_CLASS_PATH_PREFIX)) {
            String jars = agentType.substring(CHILD_CLASS_PATH_PREFIX.length());
            List<URL> urls = getJarUrls(jars);
            for (URL url : urls) {
                if (logger.isDebugEnabled()) {
                    logger.debug("child-runner lib:" + url);
                }
            }
            return new PluginTestClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
        }
        return ClassLoader.getSystemClassLoader();
    }

    private static Result executeTestCase(Callable<Result> callable, ClassLoader classLoader) throws Exception {
        final Thread currentThread = Thread.currentThread();
        ClassLoader old = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(classLoader);
        try {
            return callable.call();
        } finally {
            currentThread.setContextClassLoader(old);
        }
    }


    private static Result runTests(Class<?> testClass, String testId) throws InitializationError {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new PrintListener());
        
        Runner runner = new ForkedPinpointPluginTestRunner(testClass, testId);
        Result result = junit.run(runner);
        
        return result;
    }


    private static List<URL> getJarUrls(String jars) throws MalformedURLException {
        String[] tokens = jars.split(File.pathSeparator);
        
        List<URL> urls = new ArrayList<URL>(tokens.length);
        for (String token : tokens) {
            File file = new File(token);
            urls.add(file.toURI().toURL());
        }
        return urls;
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
