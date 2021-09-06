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

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author emeroad
 */
public class DefaultProfilerConfigTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void readProperty() throws IOException {
        String path = DefaultProfilerConfig.class.getResource("/com/navercorp/pinpoint/bootstrap/config/test.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = DefaultProfilerConfig.load(path);
    }



    @Test
    public void ioBuffering_test() throws IOException {
        String path = DefaultProfilerConfig.class.getResource("/com/navercorp/pinpoint/bootstrap/config/test.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = DefaultProfilerConfig.load(path);

        Assert.assertEquals(profilerConfig.isIoBufferingEnable(), false);
        Assert.assertEquals(profilerConfig.getIoBufferingBufferSize(), 30);
    }

    @Test
    public void ioBuffering_default() throws IOException {
        String path = DefaultProfilerConfig.class.getResource("/com/navercorp/pinpoint/bootstrap/config/default.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = DefaultProfilerConfig.load(path);

        Assert.assertEquals(profilerConfig.isIoBufferingEnable(), true);
        Assert.assertEquals(profilerConfig.getIoBufferingBufferSize(), 10);
    }


    @Test
    public void readList() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("profiler.test.list1", "foo,bar");
        properties.setProperty("profiler.test.list2", "foo, bar");
        properties.setProperty("profiler.test.list3", " foo,bar");
        properties.setProperty("profiler.test.list4", "foo,bar ");
        properties.setProperty("profiler.test.list5", "    foo,    bar   ");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);

        MatcherAssert.assertThat(profilerConfig.readList("profiler.test.list1"), CoreMatchers.hasItems("foo", "bar"));
        MatcherAssert.assertThat(profilerConfig.readList("profiler.test.list2"), CoreMatchers.hasItems("foo", "bar"));
        MatcherAssert.assertThat(profilerConfig.readList("profiler.test.list3"), CoreMatchers.hasItems("foo", "bar"));
        MatcherAssert.assertThat(profilerConfig.readList("profiler.test.list4"), CoreMatchers.hasItems("foo", "bar"));
    }
}
