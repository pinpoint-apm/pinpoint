package com.navercorp.pinpoint.web.starter;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.web.PinpointBasicLoginConfig;
import com.navercorp.pinpoint.web.WebAppPropertySources;
import com.navercorp.pinpoint.web.WebMvcConfig;
import com.navercorp.pinpoint.web.WebServerConfig;
import com.navercorp.pinpoint.web.WebStarter;
import com.navercorp.pinpoint.web.cache.CacheConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, TransactionAutoConfiguration.class,
        SecurityAutoConfiguration.class})
@ImportResource({"classpath:applicationContext-web.xml", "classpath:servlet-context-web.xml"})
@Import({WebAppPropertySources.class, WebServerConfig.class, WebMvcConfig.class, CacheConfiguration.class})
public class WebApp  {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(com.navercorp.pinpoint.web.WebApp.class);

    public static void main(String[] args) {
        try {
            WebStarter starter = new WebStarter(com.navercorp.pinpoint.web.WebApp.class, PinpointBasicLoginConfig.class);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[WebApp] could not launch app.", exception);
        }
    }


}
