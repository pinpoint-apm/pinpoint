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
import com.navercorp.pinpoint.profiler.monitor.metric.gc.GarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.UnknownGarbageCollectorMetric;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;

/**
 * @author HyunGil Jeong
 */
public class JvmInformationProvider implements Provider<JvmInformation> {

    private final String jvmVersion;
    private final GarbageCollectorMetric garbageCollectorMetric;


    @Inject
    public JvmInformationProvider(GarbageCollectorMetric garbageCollectorMetric) {
        this.jvmVersion = JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VERSION);
        this.garbageCollectorMetric = garbageCollectorMetric;
    }

    public JvmInformationProvider() {
        this(new UnknownGarbageCollectorMetric());
    }

    public JvmInformation get() {
        TJvmGcType gcType = garbageCollectorMetric.getGcType();
        return new JvmInformation(jvmVersion, gcType.getValue());
    }
}
