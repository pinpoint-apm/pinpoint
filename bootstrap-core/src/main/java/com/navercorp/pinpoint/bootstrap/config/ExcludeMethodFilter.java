package com.navercorp.pinpoint.bootstrap.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ExcludeMethodFilter implements Filter<String> {
    private final Set<String> excludeMethods;

    public ExcludeMethodFilter(String excludeFormat) {
        this(excludeFormat, ",");
    }

    public ExcludeMethodFilter(String excludeFormat, String separator) {
        if (excludeFormat == null || excludeFormat.isEmpty()) {
            this.excludeMethods = Collections.emptySet();
            return;
        }

        final String[] split = excludeFormat.split(separator);
        this.excludeMethods = new HashSet<String>();
        for (String method : split) {
            if (isEmpty(method)) {
                continue;
            }
            method = method.trim();
            if (method.isEmpty()) {
                continue;
            }
            excludeMethods.add(method.toUpperCase());
        }
    }

    private boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    @Override
    public boolean filter(String value) {
        return excludeMethods.contains(value);
    }
}
