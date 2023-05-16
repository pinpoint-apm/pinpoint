package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.server.starter.BasicStarter;

public class WebStarter extends BasicStarter  {

    public static final String EXTERNAL_PROPERTY_SOURCE_NAME = "WebExternalEnvironment";
    public static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.web.config.location";

    public WebStarter(Class<?>... sources) {
        super(sources);
        this.externalPropertySourceName = EXTERNAL_PROPERTY_SOURCE_NAME;
        this.externalConfigurationKey = EXTERNAL_CONFIGURATION_KEY;
    }

}
