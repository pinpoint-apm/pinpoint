package com.navercorp.pinpoint.collector.starter.multi.application;

import com.navercorp.pinpoint.collector.CollectorAppPropertySources;
import com.navercorp.pinpoint.collector.config.FlinkContextConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        TransactionAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class,
        SpringDataWebAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class
})
@ImportResource({"classpath:applicationContext-collector.xml", "classpath:servlet-context-collector.xml"})
@Import({CollectorAppPropertySources.class, FlinkContextConfiguration.class})
@ComponentScan({})
public class BasicCollectorApp {
}
