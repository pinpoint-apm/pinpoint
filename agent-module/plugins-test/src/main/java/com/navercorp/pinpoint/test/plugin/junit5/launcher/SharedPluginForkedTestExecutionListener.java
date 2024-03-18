/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin.junit5.launcher;

import com.navercorp.pinpoint.test.plugin.ExceptionWriter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.JUNIT_OUTPUT_DELIMITER;
import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.UTF_8_NAME;

public class SharedPluginForkedTestExecutionListener implements TestExecutionListener {
    private static final String ENGINE_ID = "[engine:junit-jupiter]";
    private static final String SEGEMENT_ID = "dependency:";

    private final ExceptionWriter writer = new ExceptionWriter();
    private String testId;

    public SharedPluginForkedTestExecutionListener(String testId) throws UnsupportedEncodingException {
        this.testId = URLEncoder.encode(testId, UTF_8_NAME);
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        System.out.println(JUNIT_OUTPUT_DELIMITER + "executionSkipped" + JUNIT_OUTPUT_DELIMITER + toReportId(testIdentifier) + JUNIT_OUTPUT_DELIMITER + reason);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        System.out.println(JUNIT_OUTPUT_DELIMITER + "executionStarted" + JUNIT_OUTPUT_DELIMITER + toReportId(testIdentifier));
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        System.out.println(JUNIT_OUTPUT_DELIMITER + "executionFinished" + JUNIT_OUTPUT_DELIMITER + toReportId(testIdentifier) + JUNIT_OUTPUT_DELIMITER + toResult(testExecutionResult));
    }

    String toReportId(TestIdentifier testIdentifier) {
        String uniqueId = testIdentifier.getUniqueId();
        if (uniqueId.startsWith(ENGINE_ID)) {
            uniqueId = uniqueId.substring(ENGINE_ID.length());
        }

        return "[" + SEGEMENT_ID + testId + "]" + uniqueId;
    }

    private String toResult(TestExecutionResult testExecutionResult) {
        String result = testExecutionResult.getStatus().toString();
        if (testExecutionResult.getThrowable().isPresent()) {
            final Throwable throwable = testExecutionResult.getThrowable().get();
            result += JUNIT_OUTPUT_DELIMITER + writer.write(throwable.getMessage(), throwable);
        }
        return result + JUNIT_OUTPUT_DELIMITER;
    }
}
