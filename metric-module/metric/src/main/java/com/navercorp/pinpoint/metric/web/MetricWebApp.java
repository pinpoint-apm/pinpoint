package com.navercorp.pinpoint.metric.web;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@ImportResource({"classpath:pinot-web/applicationContext-web-pinot.xml"})
@Import(WebMetricPropertySources.class)
@Profile("metric")
public class MetricWebApp {
}
