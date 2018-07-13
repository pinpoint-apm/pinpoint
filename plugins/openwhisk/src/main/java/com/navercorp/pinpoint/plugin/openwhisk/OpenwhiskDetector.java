package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author upgle (Seonghyun, Oh)
 */
public class OpenwhiskDetector implements ApplicationTypeDetector {

    private static final String CONTROLLER_REQUIRED_CLASS = "whisk.core.controller.Controller";

    private static final String INVOKER_REQUIRED_CLASS = "whisk.core.invoker.Invoker";

    private ServiceType applicationType = OpenwhiskConstants.OPENWHISK_INTERNAL;

    @Override
    public ServiceType getApplicationType() {
        return this.applicationType;
    }

    @Override
    public boolean detect(ConditionProvider provider) {
        setOpenwhiskApplicationType(provider);
        return true;
    }

    private void setOpenwhiskApplicationType(ConditionProvider provider) {
        if (provider.checkForClass(CONTROLLER_REQUIRED_CLASS)) {
            this.applicationType = OpenwhiskConstants.OPENWHISK_CONTROLLER;
        } else if (provider.checkForClass(INVOKER_REQUIRED_CLASS)) {
            this.applicationType = OpenwhiskConstants.OPENWHISK_INVOKER;
        }
    }

}
