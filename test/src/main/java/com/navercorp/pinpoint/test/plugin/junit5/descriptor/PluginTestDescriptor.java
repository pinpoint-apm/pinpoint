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

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.JupiterTestDescriptor;
import org.junit.jupiter.engine.execution.ConditionEvaluator;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public abstract class PluginTestDescriptor extends AbstractTestDescriptor implements Node<JupiterEngineExecutionContext> {

    public static final String ENGINE_ID = "pinpoint-plugin";

    private static final Logger logger = LoggerFactory.getLogger(JupiterTestDescriptor.class);

    private static final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();

    final JupiterConfiguration configuration;

    PluginTestDescriptor(UniqueId uniqueId, String displayName, TestSource source,
                         JupiterConfiguration configuration) {
        super(uniqueId, displayName, source);
        this.configuration = configuration;
    }

    // --- TestDescriptor ------------------------------------------------------

    // --- Node ----------------------------------------------------------------

    @Override
    public ExecutionMode getExecutionMode() {
        return ExecutionMode.SAME_THREAD;
    }

    @Override
    public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) throws Exception {
        context.getThrowableCollector().assertEmpty();
        ConditionEvaluationResult evaluationResult = conditionEvaluator.evaluate(context.getExtensionRegistry(),
                context.getConfiguration(), context.getExtensionContext());
        return toSkipResult(evaluationResult);
    }

    private SkipResult toSkipResult(ConditionEvaluationResult evaluationResult) {
        if (evaluationResult.isDisabled()) {
            return SkipResult.skip(evaluationResult.getReason().orElse("<unknown>"));
        }
        return SkipResult.doNotSkip();
    }

    /**
     * Must be overridden and return a new context so cleanUp() does not accidentally close the parent context.
     */
    @Override
    public abstract JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) throws Exception;

    @Override
    public void cleanUp(JupiterEngineExecutionContext context) throws Exception {
        context.close();
    }

    /**
     * @since 5.5
     */
    @FunctionalInterface
    interface ExceptionHandlerInvoker<E extends Extension> {

        /**
         * Invoke the supplied {@code exceptionHandler} with the supplied {@code throwable}.
         */
        void invoke(E exceptionHandler, Throwable throwable) throws Throwable;

    }
}
