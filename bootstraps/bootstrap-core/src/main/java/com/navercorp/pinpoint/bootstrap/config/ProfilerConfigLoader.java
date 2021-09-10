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

import com.navercorp.pinpoint.bootstrap.config.util.ValueAnnotationProcessor;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProfilerConfigLoader {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(DefaultProfilerConfig.class.getName());

    public static ProfilerConfig load(InputStream inputStream) {
        Properties properties = loadProperties(inputStream);
        return load(properties);
    }

    private static Properties loadProperties(InputStream inputStream) {
        try {
            return PropertyUtils.loadProperty(inputStream);
        } catch (IOException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("IO error. Error:" + ex.getMessage(), ex);
            }
            throw new RuntimeException("IO error. Error:" + ex.getMessage(), ex);
        }
    }

    public static ProfilerConfig load(Properties properties) {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        loadPropertyValues(profilerConfig, properties);
        return profilerConfig;
    }

    // for test
    private static void loadPropertyValues(ProfilerConfig profilerConfig, Properties properties) {
        ValueAnnotationProcessor processor = new ValueAnnotationProcessor();
        processor.process(profilerConfig, properties);
    }
}
