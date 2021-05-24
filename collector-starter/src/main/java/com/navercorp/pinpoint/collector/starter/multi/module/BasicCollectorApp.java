package com.navercorp.pinpoint.collector.starter.multi.module;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ImportResource;


@Deprecated
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ImportResource({ "classpath:applicationContext-collector.xml", "classpath:servlet-context-collector.xml"})
public class BasicCollectorApp {
}
