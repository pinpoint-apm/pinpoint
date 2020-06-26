package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

public class Descriptor {
    private final String handlerId;
    private final List<String> classNames;
    private final List<String> containsExceptionMessages;
    private final boolean nestedSearch;
    private final boolean parentSearch;

    public Descriptor(String handlerId, List<String> classNames, List<String> containsExceptionMessages, boolean nestedSearch, boolean parentSearch) {
        this.handlerId = Assert.requireNonNull(handlerId, "handlerId");
        this.classNames = Assert.requireNonNull(classNames, "classNames");
        this.containsExceptionMessages = Assert.requireNonNull(containsExceptionMessages, "containsExceptionMessages");
        this.nestedSearch = nestedSearch;
        this.parentSearch = parentSearch;
    }

    public String getHandlerId() {
        return handlerId;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public List<String> getContainsExceptionMessages() {
        return containsExceptionMessages;
    }

    public boolean isNestedSearch() {
        return nestedSearch;
    }

    public boolean isParentSearch() {
        return parentSearch;
    }

    @Override
    public String toString() {
        return "Descriptor{" +
                "handlerId='" + handlerId + '\'' +
                ", classNames=" + classNames +
                ", containsExceptionMessages=" + containsExceptionMessages +
                ", nestedSearch=" + nestedSearch +
                ", parentSearch=" + parentSearch +
                '}';
    }
}
