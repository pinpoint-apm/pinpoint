package com.navercorp.pinpoint.uristat.web;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@ImportResource({"classpath:applicationContext-web-uristat.xml"})
@Import(UriStatWebPropertySources.class)
@Profile("metric")
public class UriStatWebConfig {
}
