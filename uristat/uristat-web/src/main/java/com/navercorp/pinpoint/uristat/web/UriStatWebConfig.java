package com.navercorp.pinpoint.uristat.web;


import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@Configuration
@ImportResource({"classpath:applicationContext-web-uristat.xml"})
@Import({UriStatWebPropertySources.class, PinotConfiguration.class})
@Profile("uri")
public class UriStatWebConfig {
}
