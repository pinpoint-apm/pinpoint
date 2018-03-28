/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.filedescriptor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.*;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Constructor;

/**
 * @author Roy Kim
 */
public class FileDescriptorMetricProvider implements Provider<FileDescriptorMetric> {

    private static final String ORACLE_FILE_DESCRIPTOR_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.oracle.DefaultFileDescriptorMetric";
    private static final String IBM_FILE_DESCRIPTOR_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.ibm.DefaultFileDescriptorMetric";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String vendorName;

    @Inject
    public FileDescriptorMetricProvider(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        vendorName = profilerConfig.getProfilerJvmVendorName();
    }

    @Override
    public FileDescriptorMetric get() {

        String classToLoad = null;
        JvmVersion jvmVersion = JvmUtils.getVersion();
        JvmType jvmType = JvmType.fromVendor(vendorName);

        OsType osType = OsUtils.getType();

        if (jvmType == JvmType.UNKNOWN) {
            jvmType = JvmUtils.getType();
        }

        if(osType == OsType.MAC || osType == OsType.SOLARIS || osType == OsType.LINUX){

            if (jvmType == JvmType.ORACLE || jvmType == JvmType.OPENJDK) {
                if(osType == OsType.LINUX){
                    logger.warn("Unsupported operating system.");
                    return FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC;
                }
                if (jvmVersion.onOrAfter(JvmVersion.JAVA_5)) {
                    classToLoad = ORACLE_FILE_DESCRIPTOR_METRIC;
                }
            } else if (jvmType == JvmType.IBM) {
                if (jvmVersion.onOrAfter(JvmVersion.JAVA_8)) {
                    classToLoad = IBM_FILE_DESCRIPTOR_METRIC;
                }
            }
        }else{
            logger.warn("Unsupported operating system.");
            return FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC;
        }

        FileDescriptorMetric fileDescriptorMetric = createFileDescriptorMetric(classToLoad);
        logger.info("loaded : {}", fileDescriptorMetric);
        return fileDescriptorMetric;
    }

    private FileDescriptorMetric createFileDescriptorMetric(String classToLoad) {
        if (classToLoad == null) {
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
            logger.warn("Error creating FileDescriptorMetric [" + classToLoad + "]", e);
            return FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC;
        }
    }
}
