package com.navercorp.pinpoint.common.server.trace;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.UUIDType;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import com.fasterxml.uuid.impl.UUIDUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class OtelServerTraceIdTest {

    TimeBasedEpochRandomGenerator gen = Generators.timeBasedEpochRandomGenerator();

    @Test
    void getId() {

        UUID uuid = gen.generate();

        byte[] uuidBytes = UUIDUtil.asByteArray(uuid);
        OtelServerTraceId otelServerTraceId = new OtelServerTraceId(uuidBytes);

        UUID uuid2 = UUIDUtil.constructUUID(UUIDType.TIME_BASED_EPOCH, otelServerTraceId.getId());

        Assertions.assertEquals(uuid, uuid2);
    }
}