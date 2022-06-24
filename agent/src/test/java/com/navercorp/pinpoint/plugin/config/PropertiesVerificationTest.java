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

package com.navercorp.pinpoint.plugin.config;

import com.navercorp.pinpoint.common.util.PropertyUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class PropertiesVerificationTest {

    @Test
    public void checkConfigTest() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("pinpoint-root.config");

        String collectorIp = properties.getProperty("profiler.collector.ip");
        Assertions.assertEquals("127.0.0.1", collectorIp);

        collectorIp = properties.getProperty("profiler.collector.span.ip");
        Assertions.assertEquals("${profiler.collector.ip}", collectorIp);

        collectorIp = properties.getProperty("profiler.collector.stat.ip");
        Assertions.assertEquals("${profiler.collector.ip}", collectorIp);

        collectorIp = properties.getProperty("profiler.collector.tcp.ip");
        Assertions.assertEquals("${profiler.collector.ip}", collectorIp);
    }

}
