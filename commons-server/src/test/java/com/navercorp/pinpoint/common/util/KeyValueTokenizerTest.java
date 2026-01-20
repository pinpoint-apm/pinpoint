package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyValueTokenizerTest {

    @Test
    void tokenize() {
        KeyValueTokenizer.KeyValue keyValue = KeyValueTokenizer.tokenize("key=value", "=");
        assertEquals("key", keyValue.getKey());
        assertEquals("value", keyValue.getValue());
    }

    @Test
    void tokenize2() {
        KeyValueTokenizer.KeyValue keyValue = KeyValueTokenizer.tokenize("key==value", "==");
        assertEquals("key", keyValue.getKey());
        assertEquals("value", keyValue.getValue());
    }

    @Test
    void tokenize_emptyValue() {
        KeyValueTokenizer.KeyValue keyValue = KeyValueTokenizer.tokenize("key=", "=");
        assertEquals("key", keyValue.getKey());
        assertEquals("", keyValue.getValue());
    }

    @Test
    void tokenize_emptyKeyValue() {
        KeyValueTokenizer.KeyValue keyValue = KeyValueTokenizer.tokenize("=", "=");
        assertEquals("", keyValue.getKey());
        assertEquals("", keyValue.getValue());
    }

    @Test
    void tokenize_empty() {
        KeyValueTokenizer.KeyValue keyValue = KeyValueTokenizer.tokenize("", "=");
        Assertions.assertNull(keyValue);
    }
}