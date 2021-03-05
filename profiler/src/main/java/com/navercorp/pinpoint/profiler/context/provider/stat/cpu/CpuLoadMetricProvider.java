/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.cpu;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.JvmType;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class CpuLoadMetricProvider implements Provider<CpuLoadMetric> {

    // Oracle
    private static final String ORACLE_CPU_LOAD_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.cpu.oracle.DefaultCpuLoadMetric";
    // IBM
    private static final String IBM_CPU_LOAD_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.cpu.ibm.DefaultCpuLoadMetric";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String vendorName;

    @Inject
    public CpuLoadMetricProvider(ProfilerConfig profilerConfig) {
        Objects.requireNonNull(profilerConfig, "profilerConfig");
        vendorName = profilerConfig.getProfilerJvmVendorName();
    }

    @Override
    public CpuLoadMetric get() {
        JvmType jvmType = JvmType.fromVendor(vendorName);
        if (jvmType == JvmType.UNKNOWN) {
            jvmType = JvmUtils.getType();
        }
        final String classToLoad = getCpuLoadMetricClassName(jvmType);
        final CpuLoadMetric cpuLoadMetric = createCpuLoadMetric(classToLoad);
        logger.info("loaded : {}", cpuLoadMetric);
        return cpuLoadMetric;
    }

    private String getCpuLoadMetricClassName(JvmType jvmType) {
        if (jvmType == JvmType.ORACLE || jvmType == JvmType.OPENJDK) {
            return ORACLE_CPU_LOAD_METRIC;
        }

        if (jvmType == JvmType.IBM) {
            return IBM_CPU_LOAD_METRIC;
        }
        return null;
    }

    private CpuLoadMetric createCpuLoadMetric(String classToLoad) {
        if (classToLoad == null) {
            return CpuLoadMetric.UNSUPPORTED_CPU_LOAD_METRIC;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<CpuLoadMetric> cpuLoadMetricClass = (Class<CpuLoadMetric>) Class.forName(classToLoad);
            try {
                Constructor<CpuLoadMetric> cpuLoadMetricConstructor = cpuLoadMetricClass.getConstructor();
                return cpuLoadMetricConstructor.newInstance();
            } catch (NoSuchMethodException e) {
                logger.warn("Unknown CpuLoadMetric : {}", classToLoad);
                return CpuLoadMetric.UNSUPPORTED_CPU_LOAD_METRIC;
            }
        } catch (Exception e) {
            logger.warn("Error creating CpuLoadMetric [" + classToLoad + "]");
            return CpuLoadMetric.UNSUPPORTED_CPU_LOAD_METRIC;
        }
    }
}
