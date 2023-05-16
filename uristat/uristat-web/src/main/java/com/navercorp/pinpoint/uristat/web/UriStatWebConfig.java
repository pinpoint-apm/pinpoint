package com.navercorp.pinpoint.uristat.web;


import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import com.navercorp.pinpoint.uristat.web.config.UriStatPinotDaoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(basePackages = "com.navercorp.pinpoint.uristat.web")
@Import({UriStatWebPropertySources.class, UriStatPinotDaoConfiguration.class, PinotConfiguration.class})
@Profile("uri")
public class UriStatWebConfig {
}
