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

import com.navercorp.pinpoint.bootstrap.resolver.condition.ClassResourceCondition;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author Seonghyun Oh
 */
public class OpenwhiskDetector {

    private static final String CONTROLLER_REQUIRED_CLASS = "org.apache.openwhisk.core.controller.Controller";

    private static final String INVOKER_REQUIRED_CLASS = "org.apache.openwhisk.core.invoker.Invoker";

    public ServiceType detectApplicationType() {
        boolean controllerClassPresent = ClassResourceCondition.INSTANCE.check(CONTROLLER_REQUIRED_CLASS);
        if (controllerClassPresent) {
            return OpenwhiskConstants.OPENWHISK_CONTROLLER;
        }
        boolean invokerClassPresent = ClassResourceCondition.INSTANCE.check(INVOKER_REQUIRED_CLASS);
        if (invokerClassPresent) {
            return OpenwhiskConstants.OPENWHISK_INVOKER;
        }
        return ServiceType.UNKNOWN;
    }
}
