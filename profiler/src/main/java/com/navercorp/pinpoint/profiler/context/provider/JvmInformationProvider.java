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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.monitor.codahale.AgentStatCollectorFactory;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.GarbageCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.UnknownGarbageCollector;

/**
 * @author HyunGil Jeong
 */
public class JvmInformationProvider implements Provider<JvmInformation> {

    private final String jvmVersion;
    private final GarbageCollector garbageCollector;


    @Inject
    public JvmInformationProvider(AgentStatCollectorFactory garbageCollector) {
        this(garbageCollector.getGarbageCollector());
    }

    public JvmInformationProvider() {
        this((GarbageCollector)null);
    }

    public JvmInformationProvider(GarbageCollector garbageCollector) {
        this.jvmVersion = JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VERSION);
        if (garbageCollector == null) {
            this.garbageCollector = new UnknownGarbageCollector();
        } else {
            this.garbageCollector = garbageCollector;
        }
    }

    public JvmInformation get() {
        return new JvmInformation(this.jvmVersion, this.garbageCollector.getTypeCode());
    }
}
