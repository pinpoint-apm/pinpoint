package com.navercorp.pinpoint.common.util;
import java.util.Objects;

public class KeyValueTokenizer {

    public static final TokenFactory<KeyValue> KEY_VALUE_FACTORY = new TokenFactory<KeyValue>() {
        public KeyValue accept(String key, String value) {
            return new KeyValue(key, value);
        }
    };

    public static final TokenFactory<KeyValue> KEY_VALUE_TRIM_FACTORY = new TokenFactory<KeyValue>() {
        public KeyValue accept(String key, String value) {
            return new KeyValue(key.trim(), value.trim());
        }
    };

    public static KeyValue tokenize(String text, String delimiter) {
        return tokenize(text, delimiter, KEY_VALUE_FACTORY);
    }

    /**
     * Tokenizes the given text into a key and value using the specified delimiter and token factory.
     *
     * @param text the text to tokenize
     * @param delimiter the delimiter separating key and value
     * @param factory the factory used to create the token
     * @param <T> the type of token to return
     * @return the parsed token, or {@code null} if the delimiter is not found in the text
     */
    public static <T> T tokenize(String text, String delimiter, TokenFactory<T> factory) {
        Objects.requireNonNull(text, "text");

        final int delimiterIndex = text.indexOf(delimiter);
        if (delimiterIndex == -1) {
            return null;
        }

        final String key = text.substring(0, delimiterIndex);

        final int delimiterLength = delimiter.length();
        if (delimiterIndex == text.length() - delimiterLength) {
            return factory.accept(key, "");
        }
        String value = text.substring(delimiterIndex + delimiterLength);
        return factory.accept(key, value);
    }


    public interface TokenFactory<V> {
        V accept(String key, String value);
    }

    public static class KeyValue {
        private final String key;
        private final String value;

        public KeyValue(String key, String value) {
            this.key = Objects.requireNonNull(key, "key");
            this.value = Objects.requireNonNull(value, "value");
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
