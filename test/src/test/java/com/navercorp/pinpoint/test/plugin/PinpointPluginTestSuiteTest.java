package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.common.Version;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PinpointPluginTestSuiteTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Dependency({})
    @TestRoot
    public static class TestCaseWithDependencyAndTestRoot {
        @Test
        public void nothing() {
        }
    }

    @Test
    public void testGivenTestCaseWithDependencyAndTestRootWhenCreateConstructorThenThrowException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("@Dependency and @TestRoot can not annotate a class at the same time");
        new PinpointPluginTestSuite(TestCaseWithDependencyAndTestRoot.class);
    }

    @Dependency(value = "com.navercorp.pinpoint:pinpoint-bootstrap-core:" + Version.VERSION)
    public static class TestCaseWithDependency {
        @Test
        public void nothing() {
        }
    }

    @Test
    public void testGivenTestCaseWithDependencyWhenSharedProcessRunThenSuccessful() throws Exception {
        final TrackingRunListener listener = new TrackingRunListener();
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);
        new PinpointPluginTestSuite(TestCaseWithDependency.class).run(notifier);
        assertThat(1, is(listener.testStartedCount.get()));
    }

    @Test
    public void testGivenTestCaseWithDependencyWhenNonSharedProcessRunThenSuccessful() throws Exception {
        final TrackingRunListener listener = new TrackingRunListener();
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);
        new PinpointPluginTestSuite(TestCaseWithDependency.class, false).run(notifier);
        assertThat(1, is(listener.testStartedCount.get()));
    }

    @TestRoot(value = "not_found_directory")
    public static class TestCaseWithTestRootEx {
        @Test
        public void nothing() {
        }
    }

    @Test
    public void testGivenTestCaseWithTestRootExWhenRunThenThrowException() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Fail to create test runners");
        final TrackingRunListener listener = new TrackingRunListener();
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);
        new PinpointPluginTestSuite(TestCaseWithTestRootEx.class).run(notifier);
    }

    @TestRoot(value = ".")
    public static class TestCaseWithTestRoot {
        @Test
        public void nothing() {
        }
    }

    @Test
    public void testGivenTestCaseWithTestRootWhenSharedProcessRunThenSuccessful() throws Exception {
        final TrackingRunListener listener = new TrackingRunListener();
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);
        new PinpointPluginTestSuite(TestCaseWithTestRoot.class).run(notifier);
        assertThat(2, is(listener.testStartedCount.get()));
    }

    @Test
    public void testGivenTestCaseWithTestRootWhenNonSharedProcessRunThenSuccessful() throws Exception {
        final TrackingRunListener listener = new TrackingRunListener();
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);
        new PinpointPluginTestSuite(TestCaseWithTestRoot.class, false).run(notifier);
        assertThat(2, is(listener.testStartedCount.get()));
    }

    @OnClassLoader(child = false)
    public static class TestCaseWithNonClassLoader {
        @Test
        public void nothing() {
        }
    }

    @Test
    public void testGivenTestCaseWithNonClassLoaderWhenRunThenThrowException() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("No test");
        final TrackingRunListener listener = new TrackingRunListener();
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);
        new PinpointPluginTestSuite(TestCaseWithNonClassLoader.class).run(notifier);
        assertThat(1, is(listener.testStartedCount.get()));
    }

    @OnClassLoader(system = true, child = false)
    public static class TestCaseWithSystemClassLoader {
        @Test
        public void nothing() {
        }
    }

    @Test
    public void testGivenTestCaseWithSystemClassLoaderWhenSharedProcessRunThenSuccessful() throws Exception {
        final TrackingRunListener listener = new TrackingRunListener();
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);
        new PinpointPluginTestSuite(TestCaseWithSystemClassLoader.class).run(notifier);
        assertThat(1, is(listener.testStartedCount.get()));
    }

    @Test
    public void testGivenTestCaseWithSystemClassLoaderWhenNonSharedProcessRunThenSuccessful() throws Exception {
        final TrackingRunListener listener = new TrackingRunListener();
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);
        new PinpointPluginTestSuite(TestCaseWithSystemClassLoader.class, false).run(notifier);
        assertThat(1, is(listener.testStartedCount.get()));
    }

    private static class TrackingRunListener extends RunListener {

        final AtomicInteger testStartedCount = new AtomicInteger();

        @Override
        public void testStarted(Description description) {
            testStartedCount.incrementAndGet();
        }
    }
}
