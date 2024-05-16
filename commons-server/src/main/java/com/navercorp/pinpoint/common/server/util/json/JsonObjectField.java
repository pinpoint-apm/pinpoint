package com.navercorp.pinpoint.common.server.util.json;

import java.util.Objects;

public class JsonObjectField<K, V> implements JsonField<K, V> {
    private final K name;
    private final V value;

    JsonObjectField(K name, V value) {
        this.name = Objects.requireNonNull(name, "name");
        this.value = value;
    }

    public K name() {
        return name;
    }

    public V value() {
        return value;
    }

    @Override
    public String toString() {
        return name.toString() + ':' + value;
    }
}
