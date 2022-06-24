package com.navercorp.pinpoint.common.server.util;

import org.junit.jupiter.api.Test;

public class ServerBootLoggerTest {

    @Test
    public void format() {
        ServerBootLogger logger = ServerBootLogger.getLogger("test");
        logger.error("error");
        logger.info("info");
    }
}