package com.navercorp.pinpoint.metric.web;


import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.metric.web",
        "com.navercorp.pinpoint.common.server.util"
})
@ImportResource({
        "classpath:/pinot-web/applicationContext-web-pinot-dao-config.xml"
})
@Import({
        WebMetricPropertySources.class,
        PinotConfiguration.class
})
@Profile("metric")
public class MetricWebApp {

}
