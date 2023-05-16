package com.navercorp.pinpoint.metric.web;


import com.navercorp.pinpoint.metric.web.config.MetricWebPinotDaoConfiguration;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.metric.web",
        "com.navercorp.pinpoint.common.server.util"
})
@Import({
        WebMetricPropertySources.class,
        MetricWebPinotDaoConfiguration.class,
        PinotConfiguration.class
})
@Profile("metric")
public class MetricWebApp {

}
