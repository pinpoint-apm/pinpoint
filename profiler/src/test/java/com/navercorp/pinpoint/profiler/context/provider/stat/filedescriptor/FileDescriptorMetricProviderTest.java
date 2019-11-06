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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.JvmType;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.common.util.OsType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;

/**
 * @author Woonduk Kang(emeroad)
 */
public class FileDescriptorMetricProviderTest {

    private final String ORACLE_FILE_DESCRIPTOR_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.oracle.DefaultFileDescriptorMetric";
    private final String IBM_FILE_DESCRIPTOR_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.ibm.DefaultFileDescriptorMetric";


    @Test
    public void testOracle_LINUX() {
        ProfilerConfig config = mock(ProfilerConfig.class);
        FileDescriptorMetricProvider fileDescriptorMetricProvider = new FileDescriptorMetricProvider(config);

        String metricClassName = fileDescriptorMetricProvider.getMetricClassName(OsType.LINUX, JvmVersion.JAVA_6, JvmType.ORACLE);
        Assert.assertEquals(ORACLE_FILE_DESCRIPTOR_METRIC, metricClassName);

        String metricClassName2 = fileDescriptorMetricProvider.getMetricClassName(OsType.AIX, JvmVersion.JAVA_6, JvmType.ORACLE);
        Assert.assertEquals(ORACLE_FILE_DESCRIPTOR_METRIC, metricClassName2);

        String metricClassName3 = fileDescriptorMetricProvider.getMetricClassName(OsType.BSD, JvmVersion.JAVA_6, JvmType.ORACLE);
        Assert.assertEquals(ORACLE_FILE_DESCRIPTOR_METRIC, metricClassName3);

    }

    @Test
    public void testIBM_SOLARIS() {
        ProfilerConfig config = mock(ProfilerConfig.class);
        FileDescriptorMetricProvider fileDescriptorMetricProvider = new FileDescriptorMetricProvider(config);

        String metricClassName = fileDescriptorMetricProvider.getMetricClassName(OsType.SOLARIS, JvmVersion.JAVA_9, JvmType.IBM);
        Assert.assertEquals(IBM_FILE_DESCRIPTOR_METRIC, metricClassName);
    }


}