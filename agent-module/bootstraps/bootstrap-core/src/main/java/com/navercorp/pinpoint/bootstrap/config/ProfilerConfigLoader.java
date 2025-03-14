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

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultJdbcOption;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcOption;
import com.navercorp.pinpoint.common.config.util.ValueAnnotationProcessor;
import com.navercorp.pinpoint.common.util.PropertyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProfilerConfigLoader {

    public static ProfilerConfig load(InputStream inputStream) {
        Properties properties = loadProperties(inputStream);
        return load(properties);
    }

    public static Properties loadProperties(InputStream inputStream) {
        try {
            return PropertyUtils.loadProperty(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("IO error. Error:" + ex.getMessage(), ex);
        }
    }

    public static ProfilerConfig load(Properties properties) {
        ValueAnnotationProcessor processor = new ValueAnnotationProcessor();

        JdbcOption jdbcOption = new DefaultJdbcOption();
        processor.process(jdbcOption, properties::getProperty);

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties, jdbcOption);
        processor.process(profilerConfig, properties::getProperty);

        return profilerConfig;
    }
}
