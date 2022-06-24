package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class OsEnvSimplePropertyTest {

    @Test
    public void test() {
        Map<String, String> map = new HashMap<>();
        String ip = "1.1.1.1";
        map.put("PROFILER_TRANSPORT_GRPC_COLLECTOR_IP", ip);
        OsEnvSimpleProperty osenv = new OsEnvSimpleProperty(map);
        Assertions.assertEquals(ip, osenv.getProperty("profiler.transport.grpc.collector.ip"));
    }

}