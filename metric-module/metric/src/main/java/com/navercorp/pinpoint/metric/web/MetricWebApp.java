package com.navercorp.pinpoint.metric.web;

import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@ImportResource({"classpath:/pinot-web/applicationContext-web-pinot.xml"})
@PropertySource({"classpath:pinot-web/jdbc-pinot.properties"})
@Profile("metric")
public class MetricWebApp {
}
