package com.navercorp.pinpoint.plugin.httpclient5;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HostUtilsTest {

    @Test
    void get() {
        String host = HostUtils.get(null);
        assertEquals("UNKNOWN", host);

        host = HostUtils.get(null, null);
        assertEquals("UNKNOWN", host);
    }
}