package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.HbaseTableNameProvider;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HbaseNamespaceConfiguration {

    private final Logger logger = LogManager.getLogger(HbaseNamespaceConfiguration.class);

    public HbaseNamespaceConfiguration() {
        logger.info("Install {}", HbaseNamespaceConfiguration.class.getSimpleName());
    }

    @Bean
    public TableNameProvider tableNameProvider(@Value("${hbase.namespace:default}") String namespace) {
        return new HbaseTableNameProvider(namespace);
    }
}
