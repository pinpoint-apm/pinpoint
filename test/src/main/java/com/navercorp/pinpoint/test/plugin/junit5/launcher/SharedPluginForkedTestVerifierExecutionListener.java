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

import com.navercorp.pinpoint.test.plugin.ReflectPluginTestVerifier;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

public class SharedPluginForkedTestVerifierExecutionListener implements TestExecutionListener {

    private final boolean manageTraceObject;

    public SharedPluginForkedTestVerifierExecutionListener(boolean manageTraceObject) {
        this.manageTraceObject = manageTraceObject;
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            ReflectPluginTestVerifier verifier = ReflectPluginTestVerifier.getInstance();
            if (verifier != null) {
                verifier.initialize(manageTraceObject);
            }
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            ReflectPluginTestVerifier verifier = ReflectPluginTestVerifier.getInstance();
            if (verifier != null) {
                verifier.cleanUp(manageTraceObject);
            }
        }
    }
}
