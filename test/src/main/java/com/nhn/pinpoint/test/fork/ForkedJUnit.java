package com.nhn.pinpoint.profiler.util;

import java.util.regex.Pattern;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class ForkedJUnit {
    static final String JUNIT_OUTPUT_DELIMETER = "#####";
    static final String JUNIT_OUTPUT_DELIMETER_REGEXP = Pattern.quote(JUNIT_OUTPUT_DELIMETER);
    private static boolean forked = false;
    
    public static boolean isForked() {
        return forked;
    }
    

    public static void main(String[] args) throws ClassNotFoundException {
        forked = true;
        
        Class<?>[] classes = new Class<?>[args.length];
        
        for (int i = 0; i < args.length; i++) {
            classes[i] = Class.forName(args[i]);
        }
        
        JUnitCore junit = new JUnitCore();
        junit.addListener(new PrintListener());
        Result result = junit.run(classes);
        
        System.exit(result.getFailureCount());
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
