package com.navercorp.pinpoint.common.server.util.json;

import java.util.Objects;

public class JsonStringField<V> implements JsonField<String, V> {
    private final String name;
    private final V value;

    JsonStringField(String name, V value) {
        this.name = Objects.requireNonNull(name, "name");
        this.value = value;
    }

    public String name() {
        return name;
    }

    public V value() {
        return value;
    }

    @Override
    public String toString() {
        return name + ':' + value;
    }
}
