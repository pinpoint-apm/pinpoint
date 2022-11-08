package com.navercorp.pinpoint.metric.web;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@ImportResource({"classpath:pinot-web/applicationContext-web-pinot.xml"})
@Import(WebMetricPropertySources.class)
@Profile("metric")
public class MetricWebApp {
}
