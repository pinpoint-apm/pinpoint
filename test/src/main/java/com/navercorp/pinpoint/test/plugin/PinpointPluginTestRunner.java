/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.test.plugin;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 * We have referred OrderedThreadPoolExecutor ParentRunner of JUnit.
 *
 * @author Jongho Moon
 * @author Taejin Koo
 *
 */
public class PinpointPluginTestRunner extends BlockJUnit4ClassRunner {
    private final PinpointPluginTestContext context;
    private final PinpointPluginTestInstance testCase;

    private final Object childrenLock = new Object();
    private volatile Collection<FrameworkMethod> filteredChildren = null;

    private volatile RunnerScheduler scheduler = new RunnerScheduler() {
        public void schedule(Runnable childStatement) {
            childStatement.run();
        }

        public void finished() {
            // do nothing
        }
    };

    PinpointPluginTestRunner(PinpointPluginTestContext context, PinpointPluginTestInstance testCase) throws InitializationError {
        super(context.getTestClass());

        this.context = context;
        this.testCase = testCase;
    }

    @Override
    protected String getName() {
        return String.format("[%s]", testCase.getTestId());
    }

    @Override
    protected String testName(final FrameworkMethod method) {
        return String.format("%s[%s]", method.getName(), testCase.getTestId());
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        return new PinpointPluginTestStatement(this, notifier, context, testCase);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        synchronized (childrenLock) {
            List<FrameworkMethod> children = new ArrayList<FrameworkMethod>(getFilteredChildren());
            for (Iterator<FrameworkMethod> iter = children.iterator(); iter.hasNext(); ) {
                FrameworkMethod each = iter.next();
                if (shouldRun(filter, each)) {
                    try {
                        filter.apply(each);
                    } catch (NoTestsRemainException e) {
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
            filteredChildren = Collections.unmodifiableCollection(children);
            if (filteredChildren.isEmpty()) {
                throw new NoTestsRemainException();
            }
        }
    }

    private Collection<FrameworkMethod> getFilteredChildren() {
        if (filteredChildren == null) {
            synchronized (childrenLock) {
                if (filteredChildren == null) {
                    filteredChildren = Collections.unmodifiableCollection(getChildren());
                }
            }
        }
        return filteredChildren;
    }

    public void sort(Sorter sorter) {
        synchronized (childrenLock) {
            for (FrameworkMethod each : getFilteredChildren()) {
                sorter.apply(each);
            }
            List<FrameworkMethod> sortedChildren = new ArrayList<FrameworkMethod>(getFilteredChildren());
            Collections.sort(sortedChildren, comparator(sorter));
            filteredChildren = Collections.unmodifiableCollection(sortedChildren);
        }
    }

    private Comparator<? super FrameworkMethod> comparator(final Sorter sorter) {
        return new Comparator<FrameworkMethod>() {
            public int compare(FrameworkMethod o1, FrameworkMethod o2) {
                return sorter.compare(describeChild(o1), describeChild(o2));
            }
        };
    }

    private void runChildren(final RunNotifier notifier) {
        final RunnerScheduler currentScheduler = scheduler;
        try {
            for (final FrameworkMethod each : getFilteredChildren()) {
                currentScheduler.schedule(new Runnable() {
                    public void run() {
                        runChild(each, notifier);
                    }
                });
            }
        } finally {
            currentScheduler.finished();
        }
    }

    protected Statement childrenInvoker(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() {
                runChildren(notifier);
            }
        };
    }

    boolean isAvaiable(Filter filter) {
        synchronized (childrenLock) {
            List<FrameworkMethod> children = new ArrayList<FrameworkMethod>(getFilteredChildren());
            for (FrameworkMethod method : children) {
                if (shouldRun(filter, method)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shouldRun(Filter filter, FrameworkMethod each) {
        if (filter.shouldRun(describeChild(each))) {
            return true;
        }

        String testDescribe = PinpointPluginTestUtils.getTestDescribe(each.getMethod());
        return testDescribe.equals(filter.describe());
    }

    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(getName(), getRunnerAnnotations());
        for (FrameworkMethod child : getFilteredChildren()) {
            description.addChild(describeChild(child));
        }
        return description;
    }

}
