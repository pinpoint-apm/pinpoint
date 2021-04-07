package com.navercorp.pinpoint.collector.starter.multi.application;

import com.navercorp.pinpoint.collector.CollectorAppPropertySources;
import com.navercorp.pinpoint.collector.config.FlinkContextConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, TransactionAutoConfiguration.class, SqlInitializationAutoConfiguration.class})
@ImportResource({"classpath:applicationContext-collector.xml", "classpath:servlet-context-collector.xml"})
@Import({CollectorAppPropertySources.class, FlinkContextConfiguration.class})
public class BasicCollectorApp {
}
