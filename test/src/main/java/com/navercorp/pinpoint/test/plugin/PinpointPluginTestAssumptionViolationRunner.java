package com.navercorp.pinpoint.test.plugin;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.util.Objects;

public class PinpointPluginTestAssumptionViolationRunner extends Runner {
    private final Description description;
    private final String message;

    public PinpointPluginTestAssumptionViolationRunner(String className, String name, String message) {
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(name, "name");
        this.description = Description.createTestDescription(className, name);
        this.message = Objects.requireNonNull(message, "message");
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.fireTestAssumptionFailed(new Failure(description, new PinpointPluginTestException(description.getClassName(), message, null)));
    }
}
