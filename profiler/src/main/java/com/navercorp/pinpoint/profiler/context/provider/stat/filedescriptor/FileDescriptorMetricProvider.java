/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.filedescriptor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.*;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Constructor;
import java.util.EnumSet;

/**
 * @author Roy Kim
 */
public class FileDescriptorMetricProvider implements Provider<FileDescriptorMetric> {

    private static final String UNSUPPORTED_METRIC = "UNSUPPORTED_FILE_DESCRIPTOR_METRIC";

    private static final String ORACLE_FILE_DESCRIPTOR_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.oracle.DefaultFileDescriptorMetric";
    private static final String IBM_FILE_DESCRIPTOR_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.ibm.DefaultFileDescriptorMetric";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String vendorName;
    private final String osName;

    @Inject
    public FileDescriptorMetricProvider(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig");
        }
        vendorName = profilerConfig.getProfilerJvmVendorName();
        osName = profilerConfig.getProfilerOSName();
    }

    @Override
    public FileDescriptorMetric get() {

        final JvmVersion jvmVersion = JvmUtils.getVersion();
        final JvmType jvmType = getJvmType();
        final OsType osType = getOsType();

        final String classToLoad = getMetricClassName(osType, jvmVersion, jvmType);

        FileDescriptorMetric fileDescriptorMetric = createFileDescriptorMetric(classToLoad);
        logger.info("loaded : {}", fileDescriptorMetric);
        return fileDescriptorMetric;
    }

    @VisibleForTesting
    String getMetricClassName(OsType osType, JvmVersion jvmVersion, JvmType jvmType) {
        if (!isSupportedOS(osType)) {
            logger.warn("Unsupported operating system {}/{}/{}", osType, jvmVersion, jvmType);
            return UNSUPPORTED_METRIC;
        }
        if (isOracleJdk(jvmType)) {
            if (jvmVersion.onOrAfter(JvmVersion.JAVA_5)) {
                return ORACLE_FILE_DESCRIPTOR_METRIC;
            }
        }

        if (jvmType == JvmType.IBM) {
            if (jvmVersion.onOrAfter(JvmVersion.JAVA_8)) {
                return IBM_FILE_DESCRIPTOR_METRIC;
            }
        }

        return UNSUPPORTED_METRIC;
    }

    private boolean isOracleJdk(JvmType jvmType) {
        EnumSet<JvmType> orackeJdk = EnumSet.of(JvmType.ORACLE, JvmType.OPENJDK);
        return orackeJdk.contains(jvmType);
    }

    private boolean isSupportedOS(OsType osType) {
        EnumSet<OsType> supportedOs = EnumSet.of(OsType.MAC, OsType.SOLARIS, OsType.LINUX
                , OsType.AIX, OsType.HP_UX, OsType.BSD);
        return supportedOs.contains(osType);
    }

    private JvmType getJvmType() {
        final JvmType jvmType = JvmType.fromVendor(vendorName);
        if (jvmType == JvmType.UNKNOWN) {
            return JvmUtils.getType();
        }
        return jvmType;
    }

    private OsType getOsType() {
        final OsType osType = OsType.fromVendor(osName);
        if (osType == OsType.UNKNOWN) {
            return OsUtils.getType();
        }
        return osType;
    }

    private FileDescriptorMetric createFileDescriptorMetric(String classToLoad) {
        if (classToLoad == null) {
            return FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC;
        }
        if (UNSUPPORTED_METRIC.equals(classToLoad)) {
            return FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC;
        }

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        if (operatingSystemMXBean == null) {
            return FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<FileDescriptorMetric> fileDescriptorMetricClass = (Class<FileDescriptorMetric>) Class.forName(classToLoad);
            try {
                Constructor<FileDescriptorMetric> fileDescriptorMetricConstructor = fileDescriptorMetricClass.getConstructor(OperatingSystemMXBean.class);
                return fileDescriptorMetricConstructor.newInstance(operatingSystemMXBean);
            } catch (NoSuchMethodException e) {
                logger.warn("Unknown FileDescriptorMetric : {}", classToLoad);
                return FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC;
            }
        } catch (Exception e) {
            logger.warn("Error creating FileDescriptorMetric [{}]", classToLoad);
            return FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC;
        }
    }
}
