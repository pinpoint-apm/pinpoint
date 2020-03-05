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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author emeroad
 */
@TestPropertySource(locations = "classpath:test-pinpoint-collector.properties")
@ContextConfiguration(classes = CollectorConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CollectorConfigurationTest {

    @Autowired
    CollectorConfiguration collectorConfiguration;

    @Test
    public void l4IpTest() {
        Assert.assertTrue(collectorConfiguration.getL4IpList().contains("127.0.0.1"));
        Assert.assertTrue(collectorConfiguration.getL4IpList().contains("192.168.0.1"));
        Assert.assertTrue(collectorConfiguration.getL4IpList().contains("255.255.255.255"));
    }

}
