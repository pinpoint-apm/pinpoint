package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.instrument.Scope;

/**
 * @author emeroad
 */
public class SimpleScope implements Scope {

    private final String name;

    private int depth = 0;

    public SimpleScope(String name) {
        this.name = name;
    }

    public int push() {
        return depth++;
    }

    public int pop() {
        return --depth;
    }

    public int depth() {
        return depth;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleScope{");
        sb.append("name=").append(name);
        sb.append('}');
        return sb.toString();
    }
}
