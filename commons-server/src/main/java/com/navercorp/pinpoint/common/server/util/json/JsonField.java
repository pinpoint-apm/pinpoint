package com.navercorp.pinpoint.common.server.util.json;

public interface JsonField<K, V> {
    K name();

    V value();

    static <K, V> JsonField<K, V> of(K name, V value) {
        return new JsonObjectField<>(name, value);
    }

}
