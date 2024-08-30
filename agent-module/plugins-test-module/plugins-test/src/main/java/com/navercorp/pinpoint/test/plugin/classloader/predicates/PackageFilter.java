package com.navercorp.pinpoint.test.plugin.classloader.predicates;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

public class PackageFilter implements Predicate<String>  {

    public final String[] filters;

    public PackageFilter(String[] filters) {
        Objects.requireNonNull(filters, "filters");
        this.filters = Arrays.copyOf(filters, filters.length);
        Arrays.sort(this.filters);
    }

    @Override
    public boolean test(String name) {
        for (String packageName : filters) {
            if (name.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }
}
