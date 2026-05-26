package com.navercorp.pinpoint.profiler.name;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameVersionTest {

    @Test
    void getVersion() {
        Assertions.assertEquals(NameVersion.v1, NameVersion.getVersion(NameVersion.v1.name()));
        Assertions.assertEquals(NameVersion.v3, NameVersion.getVersion(NameVersion.v3.name()));
    }

    @Test
    void getVersion_default() {
        Assertions.assertEquals(NameVersion.v3, NameVersion.getVersion("unknown"));
    }
}