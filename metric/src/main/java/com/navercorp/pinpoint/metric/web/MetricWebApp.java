package com.navercorp.pinpoint.metric.web;

import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@ImportResource({ "classpath:/pinot-web/applicationContext-web-pinot.xml"})
@Profile("metric")
public class MetricWebApp {
}
