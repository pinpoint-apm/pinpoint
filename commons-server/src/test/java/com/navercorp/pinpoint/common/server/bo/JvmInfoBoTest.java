package com.navercorp.pinpoint.common.server.bo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JvmInfoBoTest {

    @Test
    void getVersion() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new JvmInfoBo(-1, "jvmVersion", "gcTypeName"));

        JvmInfoBo jvm0 = new JvmInfoBo(0, "jvmVersion", "gcTypeName");
        JvmInfoBo jvm1 = new JvmInfoBo(255, "jvmVersion", "gcTypeName");
        assertEquals(0, jvm0.getVersion());
        assertEquals(255, jvm1.getVersion());
    }


    @Test
    void readJvmInfo() {

        JvmInfoBo jvm0 = new JvmInfoBo(0, "jvmVersion", "gcTypeName");
        byte[] bytes = jvm0.writeValue();
        JvmInfoBo jvm1 = JvmInfoBo.readJvmInfo(bytes);

        assertEquals(jvm0.getVersion(), jvm1.getVersion());
    }
}