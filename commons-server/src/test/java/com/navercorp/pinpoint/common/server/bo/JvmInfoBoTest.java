package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.util.ByteUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JvmInfoBoTest {

    @Test
    void getVersion() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new JvmInfoBo(-1, "jvmVersion", "gcTypeName"));

        JvmInfoBo jvm0 = new JvmInfoBo(0, "jvmVersion", "gcTypeName");
        assertEquals(0, jvm0.getVersion());
        assertEquals(0, jvm0.getRawVersion());

        JvmInfoBo jvm1 = new JvmInfoBo(255, "jvmVersion", "gcTypeName");
        assertEquals(255, jvm1.getVersion());
        assertEquals(ByteUtils.toUnsignedByte(255), jvm1.getRawVersion());
    }


    @Test
    void readJvmInfo() {
        assertJvmInfo(ByteUtils.UNSIGNED_BYTE_MIN_VALUE);
        assertJvmInfo(Byte.MAX_VALUE);
        assertJvmInfo(Byte.MAX_VALUE + 1);
        assertJvmInfo(ByteUtils.UNSIGNED_BYTE_MAX_VALUE);
    }


    private void assertJvmInfo(int version) {
        JvmInfoBo jvm0 = new JvmInfoBo(version, "jvmVersion", "gcTypeName");
        byte[] bytes = jvm0.writeValue();
        JvmInfoBo jvm1 = JvmInfoBo.readJvmInfo(bytes);

        assertEquals(jvm0.getVersion(), jvm1.getVersion());
    }
}