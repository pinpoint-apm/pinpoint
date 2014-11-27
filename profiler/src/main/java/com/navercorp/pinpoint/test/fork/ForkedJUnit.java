package com.nhn.pinpoint.test.fork;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class ForkedJUnit {
    static final String CHILD_CLASS_PATH_PREFIX = "-child=";
    static final String JUNIT_OUTPUT_DELIMETER = "#####";
    static final String JUNIT_OUTPUT_DELIMETER_REGEXP = Pattern.quote(JUNIT_OUTPUT_DELIMETER);
    private static boolean forked = false;
    
    public static boolean isForked() {
        return forked;
    }
    

    public static void main(String[] args) throws ClassNotFoundException, MalformedURLException {
        forked = true;
        
        int from = 0;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        
        if (args[0].startsWith(CHILD_CLASS_PATH_PREFIX)) {
            String jars = args[0].substring(CHILD_CLASS_PATH_PREFIX.length());
            List<URL> urls = getJarUrls(jars);
            classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), classLoader);
            from = 1;
        }
        
        List<String> testClassNames = Arrays.asList(args).subList(from, args.length);
        List<Class<?>> classes = loadTestClasses(classLoader, testClassNames);
        
        Result result = runTests(classes);
        
        System.exit(result.getFailureCount());
    }


    private static Result runTests(List<Class<?>> classes) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new PrintListener());
        Result result = junit.run(classes.toArray(new Class<?>[classes.size()]));
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


    private static List<Class<?>> loadTestClasses(ClassLoader classLoader, List<String> testClassNames) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>(testClassNames.size());

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        
        for (String testClassName : testClassNames) {
            classes.add(classLoader.loadClass(testClassName));
        }

        Thread.currentThread().setContextClassLoader(old);
        return classes;
    }
    
    private static class PrintListener extends RunListener {

        @Override
        public void testRunStarted(Description description) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMETER + "testRunStarted");
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMETER + "testRunFinished");
        }

        @Override
        public void testStarted(Description description) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMETER + "testStarted" + JUNIT_OUTPUT_DELIMETER + description.getDisplayName());
        }

        @Override
        public void testFinished(Description description) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMETER + "testFinished" + JUNIT_OUTPUT_DELIMETER + description.getDisplayName());
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMETER + "testFailure" + JUNIT_OUTPUT_DELIMETER + failureToString(failure));
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            System.out.println(JUNIT_OUTPUT_DELIMETER + "testAssumptionFailure" + JUNIT_OUTPUT_DELIMETER + failureToString(failure));
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            System.out.println(JUNIT_OUTPUT_DELIMETER + "testIgnored" + JUNIT_OUTPUT_DELIMETER + description.getDisplayName());
        }
        
        private String failureToString(Failure failure) {
            StringBuilder builder = new StringBuilder();
            
            builder.append(failure.getTestHeader());
            builder.append(JUNIT_OUTPUT_DELIMETER);
            builder.append(failure.getException().getClass().getName());
            builder.append(JUNIT_OUTPUT_DELIMETER);
            builder.append(failure.getMessage());
            builder.append(JUNIT_OUTPUT_DELIMETER);
            
            for (StackTraceElement e : failure.getException().getStackTrace()) {
                builder.append(e.getClassName());
                builder.append(',');
                builder.append(e.getMethodName());
                builder.append(',');
                builder.append(e.getFileName());
                builder.append(',');
                builder.append(e.getLineNumber());
                
                builder.append(JUNIT_OUTPUT_DELIMETER);
            }
            
            return builder.toString();
        }
    }

}
