package com.navercorp.pinpoint.collector.starter.multi.application;

import com.navercorp.pinpoint.collector.PinpointCollectorModule;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, TransactionAutoConfiguration.class, SqlInitializationAutoConfiguration.class})
@Import({PinpointCollectorModule.class})
public class BasicCollectorApp {
}
