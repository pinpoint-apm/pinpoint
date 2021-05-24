package com.navercorp.pinpoint.collector.starter.multi.module;

import com.navercorp.pinpoint.common.server.profile.ProfileApplicationListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.metric.collector.MetricCollectorApp;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@Deprecated
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class MultiModuleApp {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(MultiModuleApp.class);

    public static void main(String[] args) {
        try {
            MultiStarter starter = new MultiStarter(MultiModuleApp.class);
            starter.addModule(BasicCollectorApp.class, new ProfileApplicationListener(), 1111);
            starter.addModule(MetricCollectorApp.class, 8081);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[CollectorApp] could not launch app.", exception);
        }
    }
}
