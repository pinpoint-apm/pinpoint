package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;


public class ParentClassThrowableMatcher implements ThrowableMatcher {
    private final ThrowableMatcher throwableMatcher;

    public ParentClassThrowableMatcher(ThrowableMatcher throwableMatcher) {
        this.throwableMatcher = Assert.requireNonNull(throwableMatcher, "throwableMatcher");

    }

    @SuppressWarnings("unchecked")
    public boolean match(Class<? extends Throwable> throwableClass) {
        while (throwableClass != null) {
            if (this.throwableMatcher.match(throwableClass)) {
                return true;
            }
            final Class<?> superclass = throwableClass.getSuperclass();
            if (!Throwable.class.isAssignableFrom(superclass)) {
                 return false;
            }
            throwableClass = (Class<? extends Throwable>) superclass;
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParentClassThrowableMatcher{");
        sb.append("throwableMatcher=").append(throwableMatcher);
        sb.append('}');
        return sb.toString();
    }
}
