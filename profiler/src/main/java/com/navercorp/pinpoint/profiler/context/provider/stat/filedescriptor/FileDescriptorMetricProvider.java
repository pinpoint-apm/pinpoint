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
import com.navercorp.pinpoint.common.util.JvmType;
import com.navercorp.pinpoint.common.util.JvmUtils;
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
        JvmType jvmType = JvmType.fromVendor(vendorName);

        if (jvmType == JvmType.UNKNOWN) {
            jvmType = JvmUtils.getType();
        }

        if (jvmType == JvmType.ORACLE) {

            classToLoad = ORACLE_FILE_DESCRIPTOR_METRIC;
        } else {
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
            } catch (NoSuchMethodException e1) {
                logger.warn("Unknown FileDescriptorMetric : {}", classToLoad);
                return FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC;
            }
        } catch (Exception e) {
            logger.warn("Error creating FileDescriptorMetric [" + classToLoad + "]", e);
            return FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC;
        }
    }
}
