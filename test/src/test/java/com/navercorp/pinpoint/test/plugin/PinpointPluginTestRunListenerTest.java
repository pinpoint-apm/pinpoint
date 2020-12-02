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

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.PrintStream;
import java.lang.reflect.Field;

import static com.navercorp.pinpoint.test.plugin.PinpointPluginTestConstants.JUNIT_OUTPUT_DELIMITER;
import static org.mockito.Mockito.*;

/**
 * @author WonChul Heo(heowc)
 */
public class PinpointPluginTestRunListenerTest {

    @Test(expected = RuntimeException.class)
    public void testUnknownEncoding() {
        new PinpointPluginTestRunListener(System.out, "null");
    }

    @Test
    public void testSuccessful() throws Exception {
        final PrintStream mockOut = mock(PrintStream.class);
        final RunListener listener = makeRunListenerWithOutFieldInjection(mockOut);

        // testRunStarted
        listener.testRunStarted(mock(Description.class));
        verify(mockOut, times(1)).println(eq(JUNIT_OUTPUT_DELIMITER + "testRunStarted"));

        // testStarted
        final Description mockDescriptionOfTestStarted = mock(Description.class);
        when(mockDescriptionOfTestStarted.getDisplayName()).thenReturn("pinpoint");
        listener.testStarted(mockDescriptionOfTestStarted);
        verify(mockOut, times(1)).println(eq(JUNIT_OUTPUT_DELIMITER + "testStarted" + JUNIT_OUTPUT_DELIMITER + "pinpoint"));

        // testIgnored
        final Description mockDescriptionOfTestIgnored = mock(Description.class);
        when(mockDescriptionOfTestIgnored.getDisplayName()).thenReturn("pinpoint!");
        listener.testIgnored(mockDescriptionOfTestIgnored);
        verify(mockOut, times(1)).println(eq(JUNIT_OUTPUT_DELIMITER + "testIgnored" + JUNIT_OUTPUT_DELIMITER + "pinpoint!"));

        // testAssumptionFailure
        listener.testAssumptionFailure(failure());
        verify(mockOut, times(1)).println(eq(JUNIT_OUTPUT_DELIMITER + "testAssumptionFailure" + JUNIT_OUTPUT_DELIMITER + "com.navercorp.pinpoint.test.plugin.PinpointPluginTestRunListenerTest#####com.navercorp.pinpoint.test.plugin.PinpointPluginTestRunListenerTest$FakeRuntimeException#####fake error#####"));

        // testFailure
        listener.testFailure(failure());
        verify(mockOut, times(1)).println(eq(JUNIT_OUTPUT_DELIMITER + "testFailure" + JUNIT_OUTPUT_DELIMITER + "com.navercorp.pinpoint.test.plugin.PinpointPluginTestRunListenerTest#####com.navercorp.pinpoint.test.plugin.PinpointPluginTestRunListenerTest$FakeRuntimeException#####fake error#####"));

        // testFinished
        final Description mockDescriptionOfTestFinished = mock(Description.class);
        when(mockDescriptionOfTestFinished.getDisplayName()).thenReturn("pinpoint@");
        listener.testFinished(mockDescriptionOfTestFinished);
        verify(mockOut, times(1)).println(eq(JUNIT_OUTPUT_DELIMITER + "testFinished" + JUNIT_OUTPUT_DELIMITER + "pinpoint@"));

        // testRunFinished
        listener.testRunFinished(mock(Result.class));
        verify(mockOut, times(1)).println(eq(JUNIT_OUTPUT_DELIMITER + "testRunFinished"));
    }

    private static RunListener makeRunListenerWithOutFieldInjection(PrintStream out) throws Exception {
        final RunListener listener = new PinpointPluginTestRunListener(System.out);
        final Field outField = listener.getClass().getDeclaredField("out");
        outField.setAccessible(true);
        outField.set(listener, out);
        return listener;
    }

    private static Failure failure() {
        return new Failure(Description.createSuiteDescription(PinpointPluginTestRunListenerTest.class),
                           new FakeRuntimeException("fake error"));
    }

    private static class FakeRuntimeException extends RuntimeException {

        FakeRuntimeException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}