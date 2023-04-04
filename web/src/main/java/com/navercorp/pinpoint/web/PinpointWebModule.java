package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.server.CommonsServerConfiguration;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.config.ClusterConfigurationFactory;
import com.navercorp.pinpoint.common.server.config.TypeLoaderConfiguration;
import com.navercorp.pinpoint.common.server.profile.StandardEnvironmentLogger;
import com.navercorp.pinpoint.web.cache.CacheConfiguration;
import com.navercorp.pinpoint.web.config.BasicLoginConfiguration;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.config.LogConfiguration;
import com.navercorp.pinpoint.web.config.ScatterChartConfig;
import com.navercorp.pinpoint.web.config.WebClusterConfig;
import com.navercorp.pinpoint.web.config.WebMysqlDataSourceConfiguration;
import com.navercorp.pinpoint.web.config.WebSocketConfig;
import com.navercorp.pinpoint.web.frontend.FrontendConfigExportConfiguration;
import com.navercorp.pinpoint.web.install.InstallModule;
import com.navercorp.pinpoint.web.webhook.WebhookModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.StandardEnvironment;

@Configuration
@ImportResource({
        "classpath:applicationContext-web.xml",
        "classpath:servlet-context-web.xml",

        "classpath:applicationContext-web-dao-config.xml",
        "classpath:applicationContext-web-websocket.xml"
})
@Import({
        WebAppPropertySources.class,
        CommonsServerConfiguration.class,
        TypeLoaderConfiguration.class,

        WebServerConfig.class,
        WebMvcConfig.class,
        WebMysqlDataSourceConfiguration.class,
        ClusterConfigurationFactory.class,
        CacheConfiguration.class,

        WebHbaseModule.class,

        WebSocketConfig.class,

        InstallModule.class,
        WebhookModule.class,
        FrontendConfigExportConfiguration.class,

        // profile "basicLogin"
        BasicLoginConfiguration.class
})
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.web.service",
        "com.navercorp.pinpoint.web.mapper",
        "com.navercorp.pinpoint.web.filter",
        "com.navercorp.pinpoint.web.view",
        "com.navercorp.pinpoint.web.applicationmap",
        "com.navercorp.pinpoint.web.query",
})
public class PinpointWebModule {
    @Bean
    public WebClusterConfig webClusterConfig() {
        return new WebClusterConfig();
    }

    @Bean
    public ConfigProperties configProperties() {
        return new ConfigProperties();
    }

    @Bean
    public LogConfiguration logConfiguration() {
        return new LogConfiguration();
    }

    @Bean
    public ScatterChartConfig scatterChartConfig() {
        return new ScatterChartConfig();
    }

    @Bean
    public StandardEnvironmentLogger standardEnvironmentLogger(StandardEnvironment env) {
        return new StandardEnvironmentLogger(env);
    }

}
