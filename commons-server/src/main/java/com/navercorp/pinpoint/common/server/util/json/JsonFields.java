package com.navercorp.pinpoint.common.server.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Iterators;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@JsonSerialize(using = JsonFields.Serializer.class)
public class JsonFields<K, V> implements Iterable<JsonField<String, V>>{

    private final Map<K, V> node;
    private final Function<K, String> nameMapper;

    JsonFields(Map<K, V> node, Function<K, String> nameMapper) {
        this.node = Objects.requireNonNull(node, "node");
        this.nameMapper = Objects.requireNonNull(nameMapper, "nameMapper");
    }

    @Override
    public String toString() {
        return node.toString();
    }

    @Override
    public Iterator<JsonField<String, V>> iterator() {
        return Iterators.transform(node.entrySet().iterator(), this::toJsonField);
    }

    private JsonField<String, V> toJsonField(Map.Entry<K, V> entry) {
        K key = entry.getKey();
        String name = this.nameMapper.apply(key);
        return new JsonStringField<>(name, entry.getValue());
    }

    public static class Serializer<K, V> extends JsonSerializer<JsonFields<K, V>> {
        @Override
        public void serialize(JsonFields<K, V> fields, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            final Function<K, String> nameMapper = fields.nameMapper;

            gen.writeStartObject();
            for (Map.Entry<K, V> entry : fields.node.entrySet()) {
                String name = nameMapper.apply(entry.getKey());
                gen.writeFieldName(name);
                gen.writeObject(entry.getValue());
            }
            gen.writeEndObject();
        }
    }

    public static <K, V> Builder<K, V> newBuilder() {
        return new Builder<>(Object::toString);
    }

    public static <K, V> Builder<K, V> newBuilder(Function<K, String> keyMapper) {
        return new Builder<>(keyMapper);
    }

    public static class Builder<K, V> {
        private final Map<K, V> node;
        private final Function<K, String> nameMapper;

        Builder(Function<K, String> nameMapper) {
            this.node = new LinkedHashMap<>();
            this.nameMapper = Objects.requireNonNull(nameMapper, "nameMapper");
        }

        public Builder<K, V> addField(K name, V value) {
            Objects.requireNonNull(name, "name");
            node.put(name, value);
            return this;
        }

        public Builder<K, V> addField(JsonField<K, V> field) {
            Objects.requireNonNull(field, "field");
            node.put(field.name(), field.value());
            return this;
        }

        public JsonFields<K, V> build() {
            return new JsonFields<>(node, nameMapper);
        }
    }

}
