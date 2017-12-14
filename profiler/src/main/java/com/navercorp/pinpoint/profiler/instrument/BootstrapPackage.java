/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument;

import java.util.Arrays;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BootstrapPackage {

    private static final List<String> BOOTSTRAP_PACKAGE_LIST = Arrays.asList("com.navercorp.pinpoint.bootstrap", "com.navercorp.pinpoint.common", "com.navercorp.pinpoint.exception");

    public boolean isBootstrapPackage(String className) {
        for (String bootstrapPackage : BOOTSTRAP_PACKAGE_LIST) {
            if (className.startsWith(bootstrapPackage)) {
                return true;
            }
        }
        return false;
    }
}
