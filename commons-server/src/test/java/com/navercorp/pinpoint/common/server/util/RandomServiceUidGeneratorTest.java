package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class RandomServiceUidGeneratorTest {

    private final long unsignedIntMax = 0xffffffffL;

    @Test
    void generate() {
        RandomServiceUidGenerator randomServiceUidGenerator = new RandomServiceUidGenerator();

        ServiceUid serviceUid = randomServiceUidGenerator.generate();
        int i = serviceUid.getUid();
        System.out.println("random generated (int): " + i);
        System.out.println("random generated (hex): " + String.format("0x%08X", i));
    }

    @Test
    void reserveDefaultUidTest() {
        //default service uid zero
        long defaultId = 0L;

        printLong("default uid 0", defaultId);
        Assertions.assertThat((int) defaultId).isEqualTo(ServiceUid.DEFAULT_SERVICE_UID.getUid());
    }

    @Test
    void reservedNegativeUidTest() {
        // reserved negative uid -1
        long uintMax = ((long) Integer.MAX_VALUE << 1) + 1L;
        long firstNegativeId = unsignedIntMax;
        long secondNegativeId = firstNegativeId - 1L;

        printLong("uintMax", uintMax);
        printLong("first negative uid", firstNegativeId);
        printLong("second negative uid", secondNegativeId);
        Assertions.assertThat(unsignedIntMax).isEqualTo(uintMax);
        Assertions.assertThat((int) firstNegativeId).isEqualTo(-1);
        Assertions.assertThat((int) secondNegativeId).isEqualTo(-2);
    }

    private void printLong(String name, long l) {
        System.out.println(name + " (long): " + l);
        System.out.println(name + " (int): " + (int) l);
        System.out.println(name + " (hex): " + String.format("0x%08X", l));
    }
}
