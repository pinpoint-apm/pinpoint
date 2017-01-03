/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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

package com.navercorp.pinpoint.plugin.resin;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * 
 * @author baiyang
 *
 */
public class ResinProviderDetector implements ApplicationTypeDetector {

    private static final String DEFAULT_BOOTSTRAP_CLASS = "com.caucho.server.resin.Resin";

    private final String bootstrapMains;

    public ResinProviderDetector(String bootstrapMains) {
        if (bootstrapMains == null) {
            this.bootstrapMains = DEFAULT_BOOTSTRAP_CLASS;
        } else {
            this.bootstrapMains = bootstrapMains;
        }
    }

    public ServiceType getApplicationType() {
        return ResinConstants.RESIN;
    }

    public boolean detect(ConditionProvider provider) {
        return provider.checkForClass(bootstrapMains);
    }

}
