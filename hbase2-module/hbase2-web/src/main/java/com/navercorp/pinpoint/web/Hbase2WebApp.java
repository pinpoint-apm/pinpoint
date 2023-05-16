package com.navercorp.pinpoint.web;


import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EntryPoint for IDE
 */
@SpringBootApplication
public class Hbase2WebApp {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(WebApp.class);

    public static void main(String[] args) {
        logger.info(String.format("%s start", Hbase2WebApp.class.getSimpleName()));

        WebApp.main(args);
    }

}
