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

package com.navercorp.pinpoint.test.plugin.junit5.descriptor;

import com.navercorp.pinpoint.test.plugin.ExceptionReader;
import com.navercorp.pinpoint.test.plugin.PluginForkedTestInstance;
import com.navercorp.pinpoint.test.plugin.junit5.engine.support.PluginTestReport;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.JUNIT_OUTPUT_DELIMITER;
import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.JUNIT_OUTPUT_DELIMITER_REGEXP;
import static org.junit.jupiter.engine.support.JupiterThrowableCollectorFactory.createThrowableCollector;

public class PluginForkedTestUnitTestDescriptor extends PluginTestDescriptor {
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create("PLUGIN-TEST");

    private final ExceptionReader reader = new ExceptionReader();
    private final Class<?> testClass;
    private final List<PluginForkedTestInstance> testInstanceList;

    static String generateDisplayNameForClass(Class<?> testClass) {
        String name = testClass.getName();
        int lastDot = name.lastIndexOf('.');
        return name.substring(lastDot + 1);
    }

    public PluginForkedTestUnitTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration, List<PluginForkedTestInstance> testInstanceList) {
        super(uniqueId, generateDisplayNameForClass(testClass), ClassSource.from(testClass), configuration);
        this.testClass = testClass;
        this.testInstanceList = testInstanceList;
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public String getLegacyReportingName() {
        return this.testClass.getName();
    }

    @Override
    public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
        ThrowableCollector throwableCollector = createThrowableCollector();

        // @formatter:off
        return context.extend()
                .withThrowableCollector(throwableCollector)
                .build();
        // @formatter:on
    }

    @Override
    public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) {
        ThrowableCollector throwableCollector = context.getThrowableCollector();
        final PluginForkedTestInstance testInstance = this.testInstanceList.get(0);
        throwableCollector.execute(() -> {
            evaluate(context, testInstance);
        });

        throwableCollector.assertEmpty();
        return context;
    }

    void evaluate(JupiterEngineExecutionContext context, PluginForkedTestInstance testInstance) throws Throwable {
        final ExtensionContext.Store store = context.getExtensionContext().getStore(NAMESPACE);
        final String rootUniqueId = getUniqueId().toString();
        try {
            boolean isAgentOutput = true;
            final List<String> pluginTestOutputList = new ArrayList<>();
            Scanner scanner = testInstance.startTest();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.endsWith("\\r")) {
                    line = line.substring(0, line.length() - 2);
                }

                if (line.startsWith(JUNIT_OUTPUT_DELIMITER)) {
                    String[] tokens = line.split(JUNIT_OUTPUT_DELIMITER_REGEXP);
                    String event = tokens[1];

                    if ("executionStarted".equals(event)) {
                        final String reportId = rootUniqueId + "/" + tokens[2];
                        final PluginTestReport report = new PluginTestReport(reportId);
                        report.setStarted(true);
                        store.put(report.getId(), report);

                        isAgentOutput = false;
                        pluginTestOutputList.clear();
                    } else if ("executionSkipped".equals(event)) {
                        final String reportId = rootUniqueId + "/" + tokens[2];
                        final PluginTestReport report = new PluginTestReport(reportId);
                        report.setSkipped(true);
                        if (tokens.length >= 4) {
                            report.setSkipReason(tokens[3]);
                        }
                        store.put(report.getId(), report);
                    } else if ("executionFinished".equals(event)) {
                        final String reportId = rootUniqueId + "/" + tokens[2];
                        final PluginTestReport report = store.get(reportId, PluginTestReport.class);
                        report.setOutput(pluginTestOutputList.toArray(new String[pluginTestOutputList.size()]));
                        pluginTestOutputList.clear();
                        isAgentOutput = true;

                        final TestExecutionResult.Status status = TestExecutionResult.Status.valueOf(tokens[3]);
                        if (status == TestExecutionResult.Status.SUCCESSFUL) {
                            report.setResult(TestExecutionResult.successful());
                        } else if (status == TestExecutionResult.Status.FAILED) {
                            List<String> stackTrace = slice(tokens);
                            Exception exception = toException(tokens[4], tokens[5], stackTrace);
                            report.setResult(TestExecutionResult.failed(exception));
                        } else if (status == TestExecutionResult.Status.ABORTED) {
                            List<String> stackTrace = slice(tokens);
                            Exception exception = toException(tokens[4], tokens[5], stackTrace);
                            report.setResult(TestExecutionResult.aborted(exception));
                        } else {
                            throw new IllegalStateException("unknown status=" + status);
                        }
                    }
                } else {
                    System.out.println(line);
                    if (isAgentOutput) {
//                        System.out.println(line);
                    } else {
                        pluginTestOutputList.add(line);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        } finally {
            testInstance.endTest();
        }
    }

    List<String> slice(String[] tokens) {
        if (tokens.length > 7) {
            String[] copy = Arrays.copyOfRange(tokens, 7, tokens.length - 1);
            return Arrays.asList(copy);
        }
        return Collections.emptyList();
    }

    private Exception toException(String message, String exceptionClass, List<String> traceInText) {
        return reader.read(exceptionClass, message, traceInText);
    }

    @Override
    public void after(JupiterEngineExecutionContext context) {
        ThrowableCollector throwableCollector = context.getThrowableCollector();
        Throwable previousThrowable = throwableCollector.getThrowable();

        // If the previous Throwable was not null when this method was called,
        // that means an exception was already thrown either before or during
        // the execution of this Node. If an exception was already thrown, any
        // later exceptions were added as suppressed exceptions to that original
        // exception unless a more severe exception occurred in the meantime.
        if (previousThrowable != throwableCollector.getThrowable()) {
            throwableCollector.assertEmpty();
        }
    }

    public Class<?> getTestClass() {
        return this.testClass;
    }
}
