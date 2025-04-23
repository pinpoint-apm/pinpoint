package com.navercorp.pinpoint.common.server.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Iterators;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Function;
import java.util.stream.Stream;

@JsonSerialize(using = JsonFields.Serializer.class)
public class JsonFields<K, V> implements Iterable<JsonField<K, V>>, RandomAccess {

    private final JsonField<K, V>[] fields;
    private final Function<K, String> nameMapper;

    JsonFields(JsonField<K, V>[] fields, Function<K, String> nameMapper) {
        this.fields = Objects.requireNonNull(fields, "node");
        this.nameMapper = Objects.requireNonNull(nameMapper, "nameMapper");
    }

    public int size() {
        return fields.length;
    }

    public JsonField<K, V> get(int index) {
        Objects.checkIndex(index, fields.length);
        return fields[index];
    }

    public boolean isEmpty() {
        return fields.length == 0;
    }

    @Override
    public Iterator<JsonField<K, V>> iterator() {
        return Iterators.forArray(fields);
    }

    public Stream<JsonField<K, V>> stream() {
        return Arrays.stream(fields);
    }

    public Stream<JsonField<String, V>> nameStream() {
        return stream()
                .map(f -> JsonField.of(nameMapper.apply(f.name()), f.value()));
    }


    public static class Serializer<K, V> extends JsonSerializer<JsonFields<K, V>> {
        @Override
        public void serialize(JsonFields<K, V> fields, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            final Function<K, String> nameMapper = fields.nameMapper;

            gen.writeStartObject();
            for (JsonField<K, V> entry : fields.fields) {
                String name = nameMapper.apply(entry.name());
                gen.writeFieldName(name);
                gen.writeObject(entry.value());
            }
            gen.writeEndObject();
        }
    }

    public static <K, V> Builder<K, V> newBuilder() {
        return new Builder<>(Object::toString);
    }

    /**
     * @param nameMapper nameMapper does not guarantee uniqueness.
     */
    public static <K, V> Builder<K, V> newBuilder(Function<K, String> nameMapper) {
        return new Builder<>(nameMapper);
    }

    @Override
    public String toString() {
        return Arrays.toString(fields);
    }

    public static class Builder<K, V> {
        private final Map<K, V> node;
        private final Function<K, String> nameMapper;
        // optional
        private boolean throwIfDuplicateKeys = false;
        private Comparator<JsonField<K, V>> comparator;

        Builder(Function<K, String> nameMapper) {
            this.node = new LinkedHashMap<>();
            this.nameMapper = Objects.requireNonNull(nameMapper, "nameMapper");
        }

        public Builder<K, V> addField(K name, V value) {
            Objects.requireNonNull(name, "name");
            final V duplicateKey = node.put(name, value);
            if (throwIfDuplicateKeys) {
                if (duplicateKey != null) {
                    throw new IllegalArgumentException("Duplicate key: " + name);
                }
            }
            return this;
        }

        public Builder<K, V> addField(JsonField<K, V> field) {
            Objects.requireNonNull(field, "field");

            this.addField(field.name(), field.value());
            return this;
        }

        public Builder<K, V> throwIfDuplicateKeys(boolean enable) {
            this.throwIfDuplicateKeys = enable;
            return this;
        }

        public Builder<K, V> comparator(Comparator<JsonField<K, V>> comparator) {
            this.comparator = Objects.requireNonNull(comparator, "comparator");
            return this;
        }

        public JsonFields<K, V> build() {
            @SuppressWarnings("unchecked")
            JsonField<K, V>[] fields = new JsonField[node.size()];
            int index = 0;
            for (Map.Entry<K, V> entry : node.entrySet()) {
                fields[index++] = JsonField.of(entry.getKey(), entry.getValue());
            }
            if (comparator != null) {
                Arrays.sort(fields, comparator);
            }
            return new JsonFields<>(fields, nameMapper);
        }
    }

}
