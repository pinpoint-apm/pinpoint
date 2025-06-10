package com.navercorp.pinpoint.bootstrap.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SystemPropertyManagerTest {

    @Test
    void testBackupAndRestore() {
        SystemPropertyManager manager = new SystemPropertyManager();
        System.setProperty("test.property", "originalValue");
        System.setProperty("foo", "bar");

        manager.backup("test.property");
        final String value = System.getProperty("test.property");
        assertNull(value, "Property should be cleared after backup");
        assertEquals("bar", System.getProperty("foo"), "Other properties should remain unchanged");

        manager.restore();
        assertEquals("originalValue", System.getProperty("test.property"));
    }

    @Test
    void testParsePropertyKeys() {
        SystemPropertyManager manager = new SystemPropertyManager();
        List<String> keys = manager.parsePropertyKeys("test.property, foo, bar");
        assertEquals(3, keys.size());
        assertTrue(keys.contains("test.property"));
        assertTrue(keys.contains("foo"));
        assertTrue(keys.contains("bar"));

        keys = manager.parsePropertyKeys("");
        assertTrue(keys.isEmpty(), "Empty input should return an empty list");

        keys = manager.parsePropertyKeys("   ");
        assertTrue(keys.isEmpty(), "Whitespace input should return an empty list");
    }
}