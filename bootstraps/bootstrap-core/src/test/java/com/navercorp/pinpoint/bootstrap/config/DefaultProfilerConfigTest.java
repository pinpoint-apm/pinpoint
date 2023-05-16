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

package com.navercorp.pinpoint.bootstrap.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author emeroad
 */
public class DefaultProfilerConfigTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void readProperty() {
        InputStream inputStream = DefaultProfilerConfig.class.getResourceAsStream("/com/navercorp/pinpoint/bootstrap/config/test.property");

        ProfilerConfigLoader.load(inputStream);
    }

    @Test
    public void readList() {
        Properties properties = new Properties();
        properties.setProperty("profiler.test.list1", "foo,bar");
        properties.setProperty("profiler.test.list2", "foo, bar");
        properties.setProperty("profiler.test.list3", " foo,bar");
        properties.setProperty("profiler.test.list4", "foo,bar ");
        properties.setProperty("profiler.test.list5", "    foo,    bar   ");

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        assertThat(profilerConfig.readList("profiler.test.list1")).contains("foo", "bar");
        assertThat(profilerConfig.readList("profiler.test.list2")).contains("foo", "bar");
        assertThat(profilerConfig.readList("profiler.test.list3")).contains("foo", "bar");
        assertThat(profilerConfig.readList("profiler.test.list4")).contains("foo", "bar");
    }
}
