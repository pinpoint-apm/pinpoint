package com.nhn.pinpoint.profiler.monitor.codahale.cpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.util.JvmVersion;
import com.nhn.pinpoint.common.util.JvmUtils;
import com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric.CpuLoadMetricSet;
import com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric.DefaultCpuLoadMetricSet;
import com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric.EmptyCpuLoadMetricSet;

/**
 * @author hyungil.jeong
 */
public class CpuLoadMetricSetSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CpuLoadMetricSetSelector.class);

    private static final String OPTIONAL_CPU_LOAD_METRIC_SET_CLASSPATH = "com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric.EnhancedCpuLoadMetricSet";

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
        // JDK 1.7 이상인지만 확인
        return JvmUtils.supportsVersion(JvmVersion.JAVA_7);
    }

    private static boolean canLoadDefault() {
        return JvmUtils.getVersion() != JvmVersion.UNSUPPORTED;
    }
}
