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

package com.navercorp.pinpoint.test.plugin.junit5.engine.support;

import org.junit.platform.engine.TestExecutionResult;

import java.util.Arrays;

public class PluginTestReport {
    private String id;
    private boolean started;
    private boolean skipped;
    private String skipReason;
    private TestExecutionResult result;
    private String[] output;

    public PluginTestReport(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    public TestExecutionResult getResult() {
        return result;
    }

    public void setResult(TestExecutionResult result) {
        this.result = result;
    }

    public String[] getOutput() {
        return output;
    }

    public void setOutput(String[] output) {
        this.output = output;
    }

    public String getSkipReason() {
        return skipReason;
    }

    public void setSkipReason(String skipReason) {
        this.skipReason = skipReason;
    }

    @Override
    public String toString() {
        return "PluginTestReport{" +
                "id='" + id + '\'' +
                ", started=" + started +
                ", skipped=" + skipped +
                ", skipReason='" + skipReason + '\'' +
                ", result=" + result +
                ", output=" + Arrays.toString(output) +
                '}';
    }
}
