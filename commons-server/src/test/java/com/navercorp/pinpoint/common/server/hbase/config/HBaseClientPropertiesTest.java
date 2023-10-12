/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.server.hbase.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HBaseClientPropertiesTest.TestConfig.class)
@TestPropertySource(properties = {
        "hbase.client.host=host",
        "hbase.client.port=1234",
        "hbase.client.znode=/hbase-zone",
        "hbase.client.properties.test=properties-test",
})
class HBaseClientPropertiesTest {

    @Autowired
    HBaseClientProperties properties;

    @Configuration
    @EnableConfigurationProperties
    static class TestConfig {
        @Bean
        @ConfigurationProperties(prefix = "hbase.client")
        public HBaseClientProperties hBaseProperties() {
            return new HBaseClientProperties();
        }
    }

    @Test
    public void test() {

        assertEquals("host", properties.getHost());
        assertEquals("/hbase-zone", properties.getZnode());
        assertEquals(1234, properties.getPort());
        Assertions.assertThat(properties.getProperties())
                .containsEntry("test", "properties-test");

    }


}