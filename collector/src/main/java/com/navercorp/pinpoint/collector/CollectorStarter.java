package com.navercorp.pinpoint.collector;

import com.navercorp.pinpoint.common.server.starter.BasicStarter;

public class CollectorStarter extends BasicStarter {

    public static final String EXTERNAL_PROPERTY_SOURCE_NAME = "CollectorExternalEnvironment";
    public static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.collector.config.location";

    public CollectorStarter(Class<?>... sources) {
        super(sources);
        this.externalPropertySourceName = EXTERNAL_PROPERTY_SOURCE_NAME;
        this.externalConfigurationKey = EXTERNAL_CONFIGURATION_KEY;

    }

}
