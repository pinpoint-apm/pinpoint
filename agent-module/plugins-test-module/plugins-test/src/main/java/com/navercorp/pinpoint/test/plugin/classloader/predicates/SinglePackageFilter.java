package com.navercorp.pinpoint.test.plugin.classloader.predicates;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Matches the classes of one package prefix. The multi-prefix variant is {@link PackageFilter}.
 */
public class SinglePackageFilter implements Predicate<String> {

    private final String packagePrefix;

    public SinglePackageFilter(String packagePrefix) {
        this.packagePrefix = Objects.requireNonNull(packagePrefix, "packagePrefix");
    }

    @Override
    public boolean test(String name) {
        return name.startsWith(packagePrefix);
    }
}
