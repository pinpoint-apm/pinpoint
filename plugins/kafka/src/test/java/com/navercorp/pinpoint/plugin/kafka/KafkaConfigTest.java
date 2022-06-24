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

package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class KafkaConfigTest {

    @Test
    public void configTest1() throws Exception {
        KafkaConfig config = createConfig("true", "true", "entryPoint");

        Assertions.assertTrue(config.isProducerEnable());
        Assertions.assertTrue(config.isConsumerEnable());
        Assertions.assertEquals("entryPoint", config.getKafkaEntryPoint());
    }

    @Test
    public void configTest2() throws Exception {
        KafkaConfig config = createConfig("true", "false");

        Assertions.assertTrue(config.isProducerEnable());
        Assertions.assertFalse(config.isConsumerEnable());
        Assertions.assertEquals("", config.getKafkaEntryPoint());
    }

    @Test
    public void configTest3() throws Exception {
        KafkaConfig config = createConfig("false", "true");

        Assertions.assertFalse(config.isProducerEnable());
        Assertions.assertTrue(config.isConsumerEnable());
        Assertions.assertEquals("", config.getKafkaEntryPoint());
    }

    @Test
    public void configTest4() throws Exception {
        KafkaConfig config = createConfig("false", "false");

        Assertions.assertFalse(config.isProducerEnable());
        Assertions.assertFalse(config.isConsumerEnable());
        Assertions.assertEquals("", config.getKafkaEntryPoint());
    }

    @Test
    public void configTest5() throws Exception {
        KafkaConfig config = createConfig("true", "false", "true", "entryPoint1");

        Assertions.assertTrue(config.isProducerEnable());
        Assertions.assertFalse(config.isConsumerEnable());
        Assertions.assertTrue(config.isHeaderEnable());
        Assertions.assertEquals("entryPoint1", config.getKafkaEntryPoint());
    }

    @Test
    public void configTest6() throws Exception {
        KafkaConfig config = createConfig("true", "false", "false", "entryPoint2");

        Assertions.assertTrue(config.isProducerEnable());
        Assertions.assertFalse(config.isConsumerEnable());
        Assertions.assertFalse(config.isHeaderEnable());
        Assertions.assertEquals("entryPoint2", config.getKafkaEntryPoint());
    }

    @Test
    public void configTest7() throws Exception {
        KafkaConfig config = createConfig("false", "true", "false", "entryPoint3");

        Assertions.assertFalse(config.isProducerEnable());
        Assertions.assertTrue(config.isConsumerEnable());
        Assertions.assertFalse(config.isHeaderEnable());
        Assertions.assertEquals("entryPoint3", config.getKafkaEntryPoint());
    }

    private KafkaConfig createConfig(String producerEnable, String consumerEnable) {
        return createConfig(producerEnable, consumerEnable, "true", "");
    }

    private KafkaConfig createConfig(String producerEnable, String consumerEnable, String consumerEntryPoint) {
        return createConfig(producerEnable, consumerEnable, "true", consumerEntryPoint);
    }

    private KafkaConfig createConfig(String producerEnable, String consumerEnable, String headerEnabled, String consumerEntryPoint) {
        Properties properties = new Properties();
        properties.put(KafkaConfig.PRODUCER_ENABLE, producerEnable);
        properties.put(KafkaConfig.CONSUMER_ENABLE, consumerEnable);
        properties.put(KafkaConfig.HEADER_ENABLE, headerEnabled);
        properties.put(KafkaConfig.CONSUMER_ENTRY_POINT, consumerEntryPoint);

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        return new KafkaConfig(profilerConfig);
    }


}
