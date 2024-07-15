package com.navercorp.pinpoint.collector.manage;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {"com.navercorp.pinpoint.collector.manage.controller"})
@Import({AdminMvcConfigure.class})
public class CollectorAdminConfiguration {
}
