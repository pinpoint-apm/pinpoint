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

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultJdbcOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author emeroad
 */
public class DefaultProfilerConfigTest {

    @Test
    public void readProperty() {
        InputStream inputStream = getClass().getResourceAsStream("/com/navercorp/pinpoint/bootstrap/config/test.property");

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

    @Test
    public void readString_placeholder() {
        Properties properties = new Properties();
        properties.setProperty("test1", "${test2}");
        properties.setProperty("test2", "2");
        properties.setProperty("test3", "3");

        properties.setProperty("PlaceholderNotExist", "${test6}");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties, new DefaultJdbcOption());

        assertThat(profilerConfig.readString("test1")).isEqualTo("2");
        assertThat(profilerConfig.readString("test2")).isEqualTo("2");
        assertThat(profilerConfig.readString("test3")).isEqualTo("3");

        assertThat(profilerConfig.readString("empty")).isNull();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            profilerConfig.readString("PlaceholderNotExist");
        });
    }

    @Test
    public void readString_placeholder_int() {
        Properties properties = new Properties();
        properties.setProperty("test1", "${test2}");
        properties.setProperty("test2", "2");
        properties.setProperty("test3", "3");

        properties.setProperty("PlaceholderNotExist", "${test6}");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties, new DefaultJdbcOption());

        assertThat(profilerConfig.readInt("test1", 0)).isEqualTo(2);
        assertThat(profilerConfig.readInt("test2", 0)).isEqualTo(2);
        assertThat(profilerConfig.readInt("test3", 0)).isEqualTo(3);

        assertThat(profilerConfig.readInt("empty", 4)).isEqualTo(4);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            profilerConfig.readLong("PlaceholderNotExist", 0);
        });
    }

    @Test
    public void readString_placeholder_long() {
        Properties properties = new Properties();
        properties.setProperty("test1", "${test2}");
        properties.setProperty("test2", "2");
        properties.setProperty("test3", "3");

        properties.setProperty("PlaceholderNotExist", "${test6}");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties, new DefaultJdbcOption());

        assertThat(profilerConfig.readLong("test1", 0)).isEqualTo(2);
        assertThat(profilerConfig.readLong("test2", 0)).isEqualTo(2);
        assertThat(profilerConfig.readLong("test3", 0)).isEqualTo(3);

        assertThat(profilerConfig.readLong("empty", 4)).isEqualTo(4);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            profilerConfig.readLong("PlaceholderNotExist", 0);
        });
    }

    @Test
    public void readString_placeholder_boolean() {
        Properties properties = new Properties();
        properties.setProperty("test1", "${test2}");
        properties.setProperty("test2", "true");
        properties.setProperty("test3", "false");

        properties.setProperty("PlaceholderNotExist", "${test6}");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties, new DefaultJdbcOption());

        assertThat(profilerConfig.readBoolean("test1", false)).isEqualTo(true);
        assertThat(profilerConfig.readBoolean("test2", false)).isEqualTo(true);
        assertThat(profilerConfig.readBoolean("test3", false)).isEqualTo(false);

        assertThat(profilerConfig.readBoolean("empty", true)).isEqualTo(true);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            profilerConfig.readBoolean("PlaceholderNotExist", true);
        });
    }

}
