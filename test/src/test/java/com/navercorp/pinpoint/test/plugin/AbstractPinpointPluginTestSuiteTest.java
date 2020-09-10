/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.Version;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author WonChul Heo(heowc)
 */
public class AbstractPinpointPluginTestSuiteTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public static class NoOpTestCase {
    }

    @Test
    public void testGivenNoOpTestCaseWhenRunThenThrowException() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Fail to create test runners");
        final Suite testSuite = new AbstractPinpointPluginTestSuite(NoOpTestCase.class) {
            @Override
            protected List<PinpointPluginTestInstance> createTestCases(PinpointPluginTestContext context) {
                assertThat(context.getAgentJar(), endsWith(String.format("/agent/target/pinpoint-agent-%s/pinpoint-bootstrap-%s.jar", Version.VERSION, Version.VERSION)));
                assertThat(context.getConfigFile(), nullValue());
                assertThat(context.getImportPluginIds(), nullValue());
                assertThat(context.getJavaExecutable(), notNullValue());
                assertThat(context.getJvmArguments(), is(new String[]{}));
                assertThat(context.getJvmVersion(), is(-1));
                assertThat(context.getMavenDependencyLibraries(), notNullValue());
                assertThat(context.getProfile(), is(PinpointProfile.DEFAULT_PROFILE));
                assertThat(context.getRequiredLibraries(), notNullValue());
                assertThat(context.getTestClass().toString(), is(NoOpTestCase.class.toString()));
                assertThat(context.getTestClassLocation(), endsWith("/pinpoint/test/target/test-classes"));
                return Collections.singletonList((PinpointPluginTestInstance) new TestPinpointPluginTestInstance());
            }
        };

        testSuite.run(new RunNotifier());
    }

    @PinpointAgent
    @PinpointConfig("pinpoint.config")
    @PinpointProfile("test")
    @JvmArgument({"-Dfile.encoding=UTF-8"})
    @JvmVersion(6)
    @BeforePinpointPluginTest
    @AfterPinpointPluginTest
    public static class OpTestCase {
        @Test
        public void nothing() {
        }
    }

    @Test
    public void testGivenOpTestCaseWhenRunThenSuccessful() throws Throwable {
        final Suite testSuite = new AbstractPinpointPluginTestSuite(OpTestCase.class) {
            @Override
            protected List<PinpointPluginTestInstance> createTestCases(PinpointPluginTestContext context) {
                assertThat(context.getAgentJar(), endsWith(String.format("/agent/target/pinpoint-agent-%s/pinpoint-bootstrap-%s.jar", Version.VERSION, Version.VERSION)));
                assertThat(context.getConfigFile(), endsWith("/test/target/test-classes/pinpoint.config"));
                assertThat(context.getImportPluginIds(), nullValue());
                assertThat(context.getJavaExecutable(), notNullValue());
                assertThat(context.getJvmArguments(), is(new String[]{ "-Dfile.encoding=UTF-8" }));
                assertThat(context.getJvmVersion(), is(6));
                assertThat(context.getMavenDependencyLibraries(), notNullValue());
                assertThat(context.getProfile(), is("test"));
                assertThat(context.getRequiredLibraries(), notNullValue());
                assertThat(context.getTestClass().toString(), is(OpTestCase.class.toString()));
                assertThat(context.getTestClassLocation(), endsWith("/test/target/test-classes"));
                return Collections.singletonList((PinpointPluginTestInstance) new TestPinpointPluginTestInstance());
            }
        };

        testSuite.run(new RunNotifier());
    }

    private static class TestPinpointPluginTestInstance implements PinpointPluginTestInstance {

        @Override
        public String getTestId() {
            return null;
        }

        @Override
        public List<String> getClassPath() {
            return null;
        }

        @Override
        public List<String> getVmArgs() {
            return null;
        }

        @Override
        public String getMainClass() {
            return null;
        }

        @Override
        public List<String> getAppArgs() {
            return null;
        }

        @Override
        public File getWorkingDirectory() {
            return null;
        }

        @Override
        public Scanner startTest() throws Throwable {
            return new Scanner("Mock Scanner");
        }

        @Override
        public void endTest() throws Throwable {

        }
    }
}
