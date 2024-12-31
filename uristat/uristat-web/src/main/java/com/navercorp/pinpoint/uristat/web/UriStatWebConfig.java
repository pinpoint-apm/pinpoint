package com.navercorp.pinpoint.uristat.web;


import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import com.navercorp.pinpoint.uristat.web.config.UriStatChartTypeConfiguration;
import com.navercorp.pinpoint.uristat.web.config.UriStatPinotDaoConfiguration;
import com.navercorp.pinpoint.uristat.web.mapper.EntityToModelMapper;
import com.navercorp.pinpoint.uristat.web.mapper.MapperConfig;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.uristat.web.controller",
        "com.navercorp.pinpoint.uristat.web.service",
        "com.navercorp.pinpoint.uristat.web.dao",
})
@Import({
        UriStatWebPropertySources.class,
        UriStatChartTypeConfiguration.class,
        UriStatPinotDaoConfiguration.class,
        MapperConfig.class,
        PinotConfiguration.class
})
@Profile("uri")
public class UriStatWebConfig {
}
