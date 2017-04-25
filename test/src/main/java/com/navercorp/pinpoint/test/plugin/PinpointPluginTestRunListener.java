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

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import com.navercorp.pinpoint.common.Charsets;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import static com.navercorp.pinpoint.test.plugin.PinpointPluginTestConstants.*;

/**
 * @author Jongho Moon
 *
 */
public class PinpointPluginTestRunListener extends RunListener {
    public static final String DEFAULT_ENCODING = Charsets.UTF_8_NAME;
    private final PrintStream out;

    public PinpointPluginTestRunListener(OutputStream out) {
        this(out, DEFAULT_ENCODING);
    }
    public PinpointPluginTestRunListener(OutputStream out, String encoding) {
        try {
            this.out = new PrintStream(out, true, encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("encoding error. error:" + ex.getMessage(), ex);
        }
    }


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
        StringBuilder builder = new StringBuilder(64);

        builder.append(failure.getTestHeader());
        builder.append(JUNIT_OUTPUT_DELIMITER);
        builder.append(failure.getException().getClass().getName());
        builder.append(JUNIT_OUTPUT_DELIMITER);
        builder.append(failure.getMessage());
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

        return builder.toString();
    }
}
