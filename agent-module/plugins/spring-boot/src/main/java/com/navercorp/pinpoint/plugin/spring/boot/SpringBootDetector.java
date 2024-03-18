/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.boot;

import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class SpringBootDetector {

    private static final String[] DEFAULT_EXPECTED_MAIN_CLASSES = {
            "org.springframework.boot.loader.launch.JarLauncher",
            "org.springframework.boot.loader.launch.WarLauncher",
            "org.springframework.boot.loader.launch.PropertiesLauncher",
            "org.springframework.boot.loader.JarLauncher",
            "org.springframework.boot.loader.WarLauncher",
            "org.springframework.boot.loader.PropertiesLauncher"
    };

    private final List<String> expectedMainClasses;

    public SpringBootDetector(List<String> expectedMainClasses) {
        if (CollectionUtils.isEmpty(expectedMainClasses)) {
            this.expectedMainClasses = Arrays.asList(DEFAULT_EXPECTED_MAIN_CLASSES);
        } else {
            this.expectedMainClasses = expectedMainClasses;
        }
    }

    public boolean detect() {
        String bootstrapMainClass = MainClassCondition.INSTANCE.getValue();
        return expectedMainClasses.contains(bootstrapMainClass);
}

}
