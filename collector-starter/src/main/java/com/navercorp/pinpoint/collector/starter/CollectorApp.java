package com.navercorp.pinpoint.collector.starter;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ImportResource({ "classpath:applicationContext-collector.xml", "classpath:servlet-context-collector.xml"})
public class CollectorApp {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(CollectorApp.class);


    public static void main(String[] args) {
        try {
            ApplicationStarter starter = new ApplicationStarter(CollectorApp.class);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[CollectorApp] could not launch app.", exception);
        }
    }

}
