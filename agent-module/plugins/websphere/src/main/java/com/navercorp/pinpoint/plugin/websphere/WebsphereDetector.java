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

import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;

import java.util.Collections;
import java.util.List;

/**
 * @author sjmittal
 * @author jaehong.kim
 */
public class WebsphereDetector {

    private static final String DEFAULT_EXPECTED_MAIN_CLASS = "com.ibm.wsspi.bootstrap.WSPreLauncher";
    private final List<String> expectedMainClasses;

    public WebsphereDetector(List<String> expectedMainClasses) {
        if (expectedMainClasses == null || expectedMainClasses.isEmpty()) {
            this.expectedMainClasses = Collections.singletonList(DEFAULT_EXPECTED_MAIN_CLASS);
        } else {
            this.expectedMainClasses = expectedMainClasses;
        }
    }

    public boolean detect() {
        String bootstrapMainClass = MainClassCondition.INSTANCE.getValue();
        return expectedMainClasses.contains(bootstrapMainClass);
    }
}
