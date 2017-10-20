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

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;
import com.navercorp.pinpoint.profiler.DefaultAgentInformation;

/**
 * @author HyunGil Jeong
 */
public class TestAgentInformation extends DefaultAgentInformation {
    
    private static final String AGENT_ID = "test-agent";
    private static final String APPLICATION_NAME = "TEST_APPLICATION";
    private static final int PID = 10;
    private static final String MACHINE_NAME = "test-machine";
    private static final String HOST_IP = "127.0.0.1";
    private static final ServiceType SERVICE_TYPE = ServiceType.TEST_STAND_ALONE;
    private static final String JVM_VERSION = JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VERSION);
    private static final String AGENT_VERSION = Version.VERSION;

    public TestAgentInformation() {
        super(AGENT_ID, APPLICATION_NAME, System.currentTimeMillis(), PID, MACHINE_NAME, HOST_IP, SERVICE_TYPE, JVM_VERSION, AGENT_VERSION);
    }
}
