package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.server.CommonsServerConfiguration;
import com.navercorp.pinpoint.common.server.profile.StandardEnvironmentLogger;
import com.navercorp.pinpoint.common.server.util.time.RangeValidatorConfiguration;
import com.navercorp.pinpoint.datasource.MainDataSourceConfiguration;
import com.navercorp.pinpoint.web.applicationmap.config.ApplicationMapModule;
import com.navercorp.pinpoint.web.cache.CacheConfiguration;
import com.navercorp.pinpoint.web.component.config.ComponentConfiguration;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.config.LogProperties;
import com.navercorp.pinpoint.web.config.ScatterChartProperties;
import com.navercorp.pinpoint.web.config.WebMysqlDaoConfiguration;
import com.navercorp.pinpoint.web.config.WebPinpointIdCacheConfiguration;
import com.navercorp.pinpoint.web.frontend.FrontendConfigExportConfiguration;
import com.navercorp.pinpoint.web.install.InstallModule;
import com.navercorp.pinpoint.web.query.QueryServiceConfiguration;
import com.navercorp.pinpoint.web.realtime.RealtimeConfig;
import com.navercorp.pinpoint.web.webhook.WebhookFacadeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.StandardEnvironment;

@Configuration
@Import({
        WebAppPropertySources.class,
        CommonsServerConfiguration.class,
        ComponentConfiguration.class,
        RangeValidatorConfiguration.class,

        WebServerConfig.class,
        WebMvcConfig.class,
        WebSocketConfig.class,
        WebServiceConfig.class,
        RealtimeConfig.class,
        MainDataSourceConfiguration.class,
        ProblemSpringWebConfig.class,

        WebPinpointIdCacheConfiguration.class,
        CacheConfiguration.class,

        ApplicationMapModule.class,
        WebHbaseModule.class,

        WebMysqlDaoConfiguration.class,

        InstallModule.class,
        WebhookFacadeModule.class,
        UserModule.class,
        FrontendConfigExportConfiguration.class,
        QueryServiceConfiguration.class,
})
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.web.mapper",
        "com.navercorp.pinpoint.web.filter",
        "com.navercorp.pinpoint.web.view",


        "com.navercorp.pinpoint.web.controller",
        "com.navercorp.pinpoint.web.util"
})
public class PinpointWebModule {

    @Bean
    public ConfigProperties configProperties() {
        return new ConfigProperties();
    }

    @Bean
    public LogProperties logProperties() {
        return new LogProperties();
    }

    @Bean
    public ScatterChartProperties scatterChartProperties() {
        return new ScatterChartProperties();
    }

    @Bean
    public StandardEnvironmentLogger standardEnvironmentLogger(StandardEnvironment env) {
        return new StandardEnvironmentLogger(env);
    }

}
