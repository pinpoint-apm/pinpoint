package com.navercorp.pinpoint.web.applicationmap.nodes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationNameEscaperTest {

    @Test
    void escape() {
        String escaped = ApplicationNameEscaper.escape("my^app");
        assertEquals("my\\^app", escaped);
    }

    @Test
    void escape_noDelimiter() {
        String escaped = ApplicationNameEscaper.escape("myapp");
        assertEquals("myapp", escaped);
    }

    @Test
    void escape_multipleDelimiters() {
        String escaped = ApplicationNameEscaper.escape("a^b^c");
        assertEquals("a\\^b\\^c", escaped);
    }

    @Test
    void escape_withBackslash() {
        String escaped = ApplicationNameEscaper.escape("my\\app");
        assertEquals("my\\\\app", escaped);
    }

    @Test
    void escape_withBackslashAndCaret() {
        String applicationName = "a\\^b";
        String escaped = ApplicationNameEscaper.escape(applicationName);
        assertEquals("a\\\\\\^b", escaped);

        String unescaped = ApplicationNameEscaper.unescape(escaped);
        assertEquals(applicationName, unescaped);
    }

    @Test
    void escape_roundTrip() {
        String original = "^string^ within a string";
        String escaped = ApplicationNameEscaper.escape(original);
        String unescaped = ApplicationNameEscaper.unescape(escaped);
        assertEquals(original, unescaped);
    }
}
