package com.navercorp.pinpoint.collector.starter.multi.application;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, TransactionAutoConfiguration.class})
@ImportResource({"classpath:applicationContext-collector.xml", "classpath:servlet-context-collector.xml"})
public class BasicCollectorApp {
}
