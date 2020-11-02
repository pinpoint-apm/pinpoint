package com.navercorp.pinpoint.metric.collector;

import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@ImportResource({ "classpath:/pinot-collector/applicationContext-collector-pinot.xml"})
@Profile("metric")
public class MetricCollectorApp {
}
