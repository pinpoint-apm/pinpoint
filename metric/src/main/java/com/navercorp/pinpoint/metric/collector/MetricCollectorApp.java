package com.navercorp.pinpoint.metric.collector;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ImportResource({ "classpath:pinot-collector/applicationContext-collector-pinot.xml", "classpath:pinot-collector/servlet-context-collector-pinot.xml"})
@Profile("metric")
public class MetricCollectorApp {
}
