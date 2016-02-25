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

package com.navercorp.pinpoint.collector.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author emeroad
 */
public class CollectorConfigurationTest {
    @Test
    public void testReadConfigFile() throws Exception {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("pinpoint-collector.properties");


    }

    @Test
    public void l4IpTest() throws Exception {
        Properties properties = new Properties();
        properties.put("collector.l4.ip", "127.0.0.1 , 192.168.0.1, 255.255.255.255");

        CollectorConfiguration collectorConfiguration = new CollectorConfiguration();
        collectorConfiguration.setProperties(properties);

        collectorConfiguration.afterPropertiesSet();

        Assert.assertTrue(collectorConfiguration.getL4IpList().contains("127.0.0.1"));
        Assert.assertTrue(collectorConfiguration.getL4IpList().contains("192.168.0.1"));
        Assert.assertTrue(collectorConfiguration.getL4IpList().contains("255.255.255.255"));
    }

}
