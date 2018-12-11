/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author Seonghyun Oh
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
        return setOpenwhiskApplicationType(provider);
    }

    private boolean setOpenwhiskApplicationType(ConditionProvider provider) {
        if (provider.checkForClass(CONTROLLER_REQUIRED_CLASS)) {
            this.applicationType = OpenwhiskConstants.OPENWHISK_CONTROLLER;
            return true;
        } else if (provider.checkForClass(INVOKER_REQUIRED_CLASS)) {
            this.applicationType = OpenwhiskConstants.OPENWHISK_INVOKER;
            return true;
        }

        return false;
    }

}
