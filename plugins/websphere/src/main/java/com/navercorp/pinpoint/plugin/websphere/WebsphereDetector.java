/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.websphere;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Arrays;
import java.util.List;

/**
 * @author sjmittal
 * @author jaehong.kim
 */
public class WebsphereDetector implements ApplicationTypeDetector {

    private static final String REQUIRED_MAIN_CLASS = "com.ibm.wsspi.bootstrap.WSPreLauncher";
    private final List<String> bootstrapMains;

    public WebsphereDetector(List<String> bootstrapMains) {
        if (bootstrapMains == null || bootstrapMains.isEmpty()) {
            this.bootstrapMains = Arrays.asList(REQUIRED_MAIN_CLASS);
        } else {
            this.bootstrapMains = bootstrapMains;
        }
    }

    @Override
    public ServiceType getApplicationType() {
        return WebsphereConstants.WEBSPHERE;
    }

    @Override
    public boolean detect(ConditionProvider provider) {
        return provider.checkMainClass(this.bootstrapMains);
    }
}
