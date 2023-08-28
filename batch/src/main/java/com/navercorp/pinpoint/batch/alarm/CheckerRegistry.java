package com.navercorp.pinpoint.batch.alarm;


import com.navercorp.pinpoint.web.alarm.CheckerCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CheckerRegistry {
    public final Map<String, AlarmCheckerFactory> registry;

    private CheckerRegistry(Map<String, AlarmCheckerFactory> registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public AlarmCheckerFactory getCheckerFactory(CheckerCategory checkerCategory) {
        Objects.requireNonNull(checkerCategory, "checkerCategory");
        return getCheckerFactory(checkerCategory.name());
    }

    public AlarmCheckerFactory getCheckerFactory(String name) {
        Objects.requireNonNull(name, "name");
        return registry.get(name);
    }


    public static CheckerRegistry.Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, AlarmCheckerFactory> registry = new HashMap<>();

        private Builder() {
        }

        public void addChecker(AlarmCheckerFactory factory) {
            Objects.requireNonNull(factory, "factory");
            AlarmCheckerFactory duplicate = this.registry.put(factory.getCategory(), factory);
            if (duplicate != null) {
                throw new IllegalStateException("Duplicate AlarmCheckerFactory " +  factory + " - "  + duplicate);
            }
        }

        public CheckerRegistry build() {
            Map<String, AlarmCheckerFactory> copy = new HashMap<>(registry);
            return new CheckerRegistry(copy);
        }

    }
}
