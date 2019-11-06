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

package com.navercorp.pinpoint.web.config;

import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class PropertiesVerificationTest {

    @Test
    public void checkHbasePropertiesTest() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("hbase.properties");

        String clientHost = properties.getProperty("hbase.client.host");
        Assert.assertEquals("localhost", clientHost);

        String clientPort = properties.getProperty("hbase.client.port");
        Assert.assertEquals("2181", clientPort);
    }

    @Test
    public void checkWebPropertiesTest() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("pinpoint-web.properties");

        String zookeeperAddress = properties.getProperty("cluster.zookeeper.address");
        Assert.assertEquals("localhost", zookeeperAddress);

        String connectAddress = properties.getProperty("cluster.connect.address");
        Assert.assertTrue(StringUtils.isEmpty(connectAddress));
    }

}
