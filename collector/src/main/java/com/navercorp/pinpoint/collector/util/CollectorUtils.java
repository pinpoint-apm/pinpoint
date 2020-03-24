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

package com.navercorp.pinpoint.collector.util;

import com.navercorp.pinpoint.common.util.IdValidateUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author koo.taejin
 */
public final class CollectorUtils {
    private static final RuntimeMXBean RUNTIME_MXBEAN = ManagementFactory.getRuntimeMXBean();

    private CollectorUtils() {
    }

    public static String getServerIdentifier() {
        // if the return value is not unique, it will be changed to MAC address or IP address.
        // It means that the return value has format of "pid@hostname" (it is possible to be duplicate for "localhost")
        return RUNTIME_MXBEAN.getName();
    }

    public static void checkAgentId(final String agentId) {
        if (!IdValidateUtils.validateId(agentId)) {
            throw new IllegalArgumentException("invalid agentId. agentId=" + agentId);
        }
    }

    public static void checkApplicationName(final String applicationName) {
        if (!IdValidateUtils.validateId(applicationName)) {
            throw new IllegalArgumentException("invalid applicationName. applicationName=" + applicationName);
        }
    }
}
