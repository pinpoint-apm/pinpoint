package com.nhn.pinpoint.context;

import org.junit.Test;

import java.util.UUID;

public class SpanIDTest {
    @Test
    public void newUUID() {
        UUID uuid = UUID.randomUUID();
        UUID id = new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());

        System.out.println(uuid);
        System.out.println(id);

//        System.out.println(uuid.node());
        uuid.compareTo(uuid);
        System.out.println(uuid.version());
        System.out.println(Long.MAX_VALUE+"-"+Long.MAX_VALUE);
    }
}
