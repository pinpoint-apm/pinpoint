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

package com.navercorp.pinpoint.profiler.monitor.codahale.cpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric.CpuLoadMetricSet;
import com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric.DefaultCpuLoadMetricSet;
import com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric.EmptyCpuLoadMetricSet;

/**
 * @author hyungil.jeong
 */
public class CpuLoadMetricSetSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CpuLoadMetricSetSelector.class);

    private static final String OPTIONAL_CPU_LOAD_METRIC_SET_CLASSPATH = "com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric.EnhancedCpuLoadMetricSet";

    private CpuLoadMetricSetSelector() {
        throw new IllegalAccessError();
    }

    public static CpuLoadMetricSet getCpuLoadMetricSet() {
        if (canLoadOptionalPackage()) {
            CpuLoadMetricSet optionalPackage = loadOptionalPackage();
            if (optionalPackage != null) {
                return optionalPackage;
            }
        }
        if (canLoadDefault()) {
            return new DefaultCpuLoadMetricSet();
        } else {
            return new EmptyCpuLoadMetricSet();
        }
    }

    private static CpuLoadMetricSet loadOptionalPackage() {
        try {
            @SuppressWarnings("unchecked")
            Class<CpuLoadMetricSet> clazz = (Class<CpuLoadMetricSet>)Class.forName(OPTIONAL_CPU_LOAD_METRIC_SET_CLASSPATH);
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                LOGGER.error("Error instantiating optional package.", e);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.info("Optional package not found.");
        }
        return null;
    }

    private static boolean canLoadOptionalPackage() {
        // Check if JDK version is >= 1.7
        return JvmUtils.supportsVersion(JvmVersion.JAVA_7);
    }

    private static boolean canLoadDefault() {
        return JvmUtils.getVersion() != JvmVersion.UNSUPPORTED;
    }
}
