package com.navercorp.pinpoint.collector.starter.multi.application;

import com.navercorp.pinpoint.collector.PinpointCollectorModule;
import com.navercorp.pinpoint.collector.event.config.CollectorEventConfiguration;
import com.navercorp.pinpoint.redis.RedisPropertySources;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        TransactionAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class,
        SpringDataWebAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class
})
@Import({
        PinpointCollectorModule.class,
        RedisPropertySources.class,
        CollectorEventConfiguration.class,
})
public class BasicCollectorApp {
}
