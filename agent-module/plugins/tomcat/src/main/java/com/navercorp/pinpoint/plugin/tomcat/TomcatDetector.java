/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.bootstrap.resolver.condition.ClassResourceCondition;
import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.bootstrap.resolver.condition.SystemPropertyCondition;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 *
 */
public class TomcatDetector {
    
    private static final String DEFAULT_EXPECTED_MAIN_CLASS = "org.apache.catalina.startup.Bootstrap";
    
    private static final String REQUIRED_SYSTEM_PROPERTY = "catalina.home";
    
    private static final String REQUIRED_CLASS = "org.apache.catalina.startup.Bootstrap";

    private final List<String> expectedMainClasses;

    public TomcatDetector(List<String> expectedMainClasses) {
        if (CollectionUtils.isEmpty(expectedMainClasses)) {
            this.expectedMainClasses = Collections.singletonList(DEFAULT_EXPECTED_MAIN_CLASS);
        } else {
            this.expectedMainClasses = expectedMainClasses;
        }
    }

    public boolean detect() {
        String bootstrapMainClass = MainClassCondition.INSTANCE.getValue();
        boolean isExpectedMainClass = expectedMainClasses.contains(bootstrapMainClass);
        if (!isExpectedMainClass) {
            return false;
        }
        boolean hasRequiredSystemProperty = SystemPropertyCondition.INSTANCE.check(REQUIRED_SYSTEM_PROPERTY);
        if (!hasRequiredSystemProperty) {
            return false;
        }
        boolean hasRequiredClass = ClassResourceCondition.INSTANCE.check(REQUIRED_CLASS);
        if (!hasRequiredClass) {
            return false;
        }
        return true;
    }

}
