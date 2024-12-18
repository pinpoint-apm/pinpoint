/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.config;

import com.navercorp.pinpoint.common.config.util.ValueAnnotationProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class DefaultContextConfigTest {

    private final ValueAnnotationProcessor processor = new ValueAnnotationProcessor();


    @Test
    public void ioBuffering_test() {
        Properties properties = new Properties();
        properties.put("profiler.io.buffering.enable", "false");
        properties.put("profiler.io.buffering.buffersize", "30");

        ContextConfig contextConfig = new DefaultContextConfig();
        processor.process(contextConfig, properties::getProperty);

        Assertions.assertFalse(contextConfig.isIoBufferingEnable());
        Assertions.assertEquals(30, contextConfig.getIoBufferingBufferSize());
    }

    @Test
    public void ioBuffering_default() {
        Properties properties = new Properties();
        properties.put("profiler.io.buffering.enable", "true");
        properties.put("profiler.io.buffering.buffersize", "10");

        ContextConfig contextConfig = new DefaultContextConfig();
        processor.process(contextConfig, properties::getProperty);

        Assertions.assertTrue(contextConfig.isIoBufferingEnable());
        Assertions.assertEquals(10, contextConfig.getIoBufferingBufferSize());
    }

}