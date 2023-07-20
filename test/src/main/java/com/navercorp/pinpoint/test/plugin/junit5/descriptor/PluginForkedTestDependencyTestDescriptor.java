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

import com.navercorp.pinpoint.test.plugin.junit5.engine.support.PluginTestReport;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

import static org.junit.jupiter.engine.support.JupiterThrowableCollectorFactory.createThrowableCollector;

public class PluginForkedTestDependencyTestDescriptor extends PluginTestDescriptor {

    private Class<?> testClass;
    private PluginTestReport testReport;

    public PluginForkedTestDependencyTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration, String displayName) {
        super(uniqueId, displayName, ClassSource.from(testClass), configuration);
        this.testClass = testClass;
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    @Override
    public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) throws Exception {
        ThrowableCollector throwableCollector = createThrowableCollector();

        final ExtensionContext.Store store = context.getExtensionContext().getStore(PluginForkedTestUnitTestDescriptor.NAMESPACE);
        this.testReport = store.get(getUniqueId().toString(), PluginTestReport.class);

        // @formatter:off
        return context.extend()
                .withThrowableCollector(throwableCollector)
                .build();
        // @formatter:on
    }

    @Override
    public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) throws Exception {
        ThrowableCollector throwableCollector = context.getThrowableCollector();
        if (testReport != null && testReport.isStarted()) {
            if (testReport.getOutput() != null) {
                for (String line : testReport.getOutput()) {
                    System.out.println(line);
                }
            }
            if (testReport.getResult().getStatus() != TestExecutionResult.Status.SUCCESSFUL) {
                throwableCollector.execute(() -> {
                    Throwable throwable = testReport.getResult().getThrowable().orElse(new IllegalStateException("unknown"));
                    throwable.printStackTrace();
                    throw throwable;
                });
            }
        }
        throwableCollector.assertEmpty();

        return context;
    }

//    @Override
//    public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
//        ThrowableCollector throwableCollector = context.getThrowableCollector();
//        if (testReport != null && testReport.isStarted()) {
//            System.out.println("##PluginForkedTestDependencyTestDescriptor=" + testReport.isStarted());
//            if (testReport.getOutput() != null) {
//                System.out.println("##PluginForkedTestDependencyTestDescriptor=" + testReport.getOutput());
//                for (String line : testReport.getOutput()) {
//                    System.out.println(line);
//                }
//            }
//            if (testReport.getResult().getStatus() != TestExecutionResult.Status.SUCCESSFUL) {
//                System.out.println("##PluginForkedTestDependencyTestDescriptor=" + testReport.getResult().getStatus());
//                throwableCollector.execute(() -> {
//                    System.out.println("##PluginForkedTestDependencyTestDescriptor=" + testReport.getResult().getThrowable().get());
//                    throw testReport.getResult().getThrowable().orElse(new IllegalStateException("unknown"));
//                });
//            }
//        } else {
//            throwableCollector.execute(() -> {
//                throw new IllegalStateException("unknown");
//            });
//        }
//        return context;
//    }

    public Class<?> getTestClass() {
        return testClass;
    }

//    @Override
//    public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) throws Exception {
//        if (testReport != null) {
//            if (testReport.isSkipped()) {
//                if (testReport.getSkipReason() != null) {
//                    return SkipResult.skip(testReport.getSkipReason());
//                }
//                return SkipResult.skip("");
//            }
//        } else {
//            return SkipResult.skip("");
//        }
//        return SkipResult.doNotSkip();
//    }
}