package com.navercorp.pinpoint.web.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.util.Properties;

@Configuration
@PropertySource(name = "WebMysqlDataSourceConfiguration", value = {
        "classpath:jdbc-root.properties",
        "classpath:profiles/${pinpoint.profiles.active:release}/jdbc.properties"
})
public class WebMysqlDataSourceConfiguration {
    private final Logger logger = LogManager.getLogger(WebMysqlDataSourceConfiguration.class);

    public WebMysqlDataSourceConfiguration() {
        logger.info("Install {}", WebMysqlDataSourceConfiguration.class.getSimpleName());
    }

    @Bean(name = "mysqlDriverProperties")
    public FactoryBean<Properties> mysqlDriverProperties(@Value("classpath:mysql-driver.properties") Resource driverProperties) {
        PropertiesFactoryBean factoryBean = new PropertiesFactoryBean();
        factoryBean.setLocation(driverProperties);
        return factoryBean;
    }

    public HikariConfig defaultHikariConfig(Properties properties) {
        HikariConfig config = new HikariConfig();
        config.setDataSourceProperties(properties);

        config.setConnectionTimeout(5000);
        config.setInitializationFailTimeout(-1);
        config.setMaxLifetime(1200000);
        config.setMaximumPoolSize(30);
        config.setMinimumIdle(30);

        return config;
    }

    @Bean
    public HikariConfig dataSourceHikariConfig(Environment env,
                                               @Qualifier("mysqlDriverProperties") Properties properties) {
        HikariConfig hikariConfig = defaultHikariConfig(properties);

        hikariConfig.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        hikariConfig.setJdbcUrl(env.getProperty("jdbc.url"));
        hikariConfig.setUsername(env.getProperty("jdbc.username"));
        hikariConfig.setPassword(env.getProperty("jdbc.password"));

        hikariConfig.setPoolName("HikariPool-dataSource");
        return hikariConfig;
    }

    @Bean
    public HikariConfig metaDataSourceHikariConfig(Environment env,
                                                   @Qualifier("mysqlDriverProperties") Properties properties) {
        HikariConfig hikariConfig = defaultHikariConfig(properties);

        hikariConfig.setDriverClassName(env.getProperty("meta.jdbc.driverClassName"));
        hikariConfig.setJdbcUrl(env.getProperty("meta.jdbc.url"));
        hikariConfig.setUsername(env.getProperty("meta.jdbc.username"));
        hikariConfig.setPassword(env.getProperty("meta.jdbc.password"));

        hikariConfig.setPoolName("HikariPool-meta");
        return hikariConfig;
    }

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(@Qualifier("dataSourceHikariConfig") HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }


    @Bean(destroyMethod = "close")
    public HikariDataSource metaDataDataSource(@Qualifier("metaDataSourceHikariConfig") HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }

}
