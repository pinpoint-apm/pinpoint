/*
 * Copyright 2026 NAVER Corp.
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

import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestClassAware;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Reuses the jupiter class lifecycle by delegation instead of inheritance: since JUnit 5.13
 * the {@code Node} lifecycle methods of {@code ClassBasedTestDescriptor} are {@code final},
 * so they can no longer be overridden. This wrapper is the node in the descriptor tree and
 * forwards to an out-of-tree {@link ClassTestDescriptor}; subclasses intercept the lifecycle
 * by overriding the forwarding methods and invoking {@code delegate} where jupiter behavior
 * is still wanted.
 */
public abstract class DelegatingClassTestDescriptor extends AbstractTestDescriptor
        implements Node<JupiterEngineExecutionContext>, TestClassAware {

    protected final ClassTestDescriptor delegate;

    DelegatingClassTestDescriptor(ClassTestDescriptor delegate) {
        super(delegate.getUniqueId(), delegate.getDisplayName(), delegate.getSource().orElse(null));
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    // --- TestClassAware --------------------------------------------------------

    @Override
    public Class<?> getTestClass() {
        return delegate.getTestClass();
    }

    @Override
    public List<Class<?>> getEnclosingTestClasses() {
        return delegate.getEnclosingTestClasses();
    }

    // --- TestDescriptor ------------------------------------------------------

    @Override
    public Type getType() {
        return delegate.getType();
    }

    /**
     * Method descriptors inherit class-level {@code @Tag}s by merging the tags of their
     * ancestors, so the wrapper must expose the delegate's tags or tag filtering breaks.
     */
    @Override
    public Set<TestTag> getTags() {
        return delegate.getTags();
    }

    @Override
    public String getLegacyReportingName() {
        return delegate.getLegacyReportingName();
    }

    @Override
    public boolean mayRegisterTests() {
        return delegate.mayRegisterTests();
    }

    // --- Node ----------------------------------------------------------------

    @Override
    public ExecutionMode getExecutionMode() {
        return delegate.getExecutionMode();
    }

    @Override
    public Set<ExclusiveResource> getExclusiveResources() {
        return delegate.getExclusiveResources();
    }

    @Override
    public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) throws Exception {
        return delegate.shouldBeSkipped(context);
    }

    @Override
    public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) throws Exception {
        return delegate.prepare(context);
    }

    @Override
    public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) throws Exception {
        return delegate.before(context);
    }

    @Override
    public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
        return delegate.execute(context, dynamicTestExecutor);
    }

    @Override
    public void after(JupiterEngineExecutionContext context) throws Exception {
        delegate.after(context);
    }

    @Override
    public void cleanUp(JupiterEngineExecutionContext context) throws Exception {
        delegate.cleanUp(context);
    }

    @Override
    public void nodeSkipped(JupiterEngineExecutionContext context, TestDescriptor testDescriptor, SkipResult result) {
        delegate.nodeSkipped(context, testDescriptor, result);
    }

    @Override
    public void nodeFinished(JupiterEngineExecutionContext context, TestDescriptor testDescriptor, TestExecutionResult result) {
        delegate.nodeFinished(context, testDescriptor, result);
    }
}
