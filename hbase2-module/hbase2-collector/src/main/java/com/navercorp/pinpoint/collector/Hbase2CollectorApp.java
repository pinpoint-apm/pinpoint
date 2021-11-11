package com.navercorp.pinpoint.collector;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Hbase2CollectorApp {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(CollectorApp.class);

    public static void main(String[] args) {
        logger.info(String.format("%s start", Hbase2CollectorApp.class.getSimpleName()));

        CollectorApp.main(args);
    }

}
