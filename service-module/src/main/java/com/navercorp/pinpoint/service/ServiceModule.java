package com.navercorp.pinpoint.service;

import com.navercorp.pinpoint.service.web.config.WebServiceConfiguration;
import org.springframework.context.annotation.Import;

@Import({
        WebServiceConfiguration.class,
})
public class ServiceModule {

}
