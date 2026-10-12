package com.navercorp.pinpoint.common.server.uid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObjectNameVersionTest {

    @Test
    void getVersion() {
        Assertions.assertEquals(ObjectNameVersion.v1, ObjectNameVersion.getVersion("v1"));
        Assertions.assertEquals(ObjectNameVersion.v3, ObjectNameVersion.getVersion("v3"));
        Assertions.assertEquals(ObjectNameVersion.v4, ObjectNameVersion.getVersion("v4"));
    }

    @Test
    void getVersion_ignoreCase() {
        Assertions.assertEquals(ObjectNameVersion.v3, ObjectNameVersion.getVersion("V3"));
    }

    @Test
    void getVersion_default() {
        Assertions.assertEquals(ObjectNameVersion.v4, ObjectNameVersion.getVersion("unknown"));
        Assertions.assertEquals(ObjectNameVersion.v4, ObjectNameVersion.getVersion(""));
        Assertions.assertEquals(ObjectNameVersion.v4, ObjectNameVersion.getVersion(null));
    }
}
