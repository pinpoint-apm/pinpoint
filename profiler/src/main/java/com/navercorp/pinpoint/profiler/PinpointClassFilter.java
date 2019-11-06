/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class PinpointClassFilter implements ClassFileFilter {

    public PinpointClassFilter() {
    }

    @Override
    public boolean accept(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (className == null) {
            return SKIP;
        }

        // Skip pinpoint packages too.
        if (className.startsWith("com/navercorp/pinpoint/")) {
            if (className.startsWith("com/navercorp/pinpoint/web/")) {
                return CONTINUE;
            }
            return SKIP;
        }

        return CONTINUE;
    }
}
