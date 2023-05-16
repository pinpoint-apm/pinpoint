/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin.shared;

import com.navercorp.pinpoint.test.plugin.ExceptionWriter;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.JUNIT_OUTPUT_DELIMITER;

public class PrintListener extends RunListener {
    private final ExceptionWriter writer = new ExceptionWriter();

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
        Throwable exception = failure.getException();
        return writer.write(failure.getTestHeader(), exception);
    }
}
