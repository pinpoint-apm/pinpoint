/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.directbuffer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.monitor.metric.directbuffer.BufferMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * @author Roy Kim
 */
public class BufferMetricProvider implements Provider<BufferMetric> {

    private static final String DIRECT_BUFFER_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.directbuffer.DefaultDirectBufferMetric";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public BufferMetricProvider() {
    }

    @Override
    public BufferMetric get() {

        final JvmVersion jvmVersion = JvmUtils.getVersion();
        if (!jvmVersion.onOrAfter(JvmVersion.JAVA_7)) {
            logger.warn("Unsupported JVM version.");
            return BufferMetric.UNSUPPORTED_BUFFER_METRIC;
        }

        BufferMetric bufferMetric = createBufferMetric(DIRECT_BUFFER_METRIC);
        logger.info("loaded : {}", bufferMetric);
        return bufferMetric;
    }

    private BufferMetric createBufferMetric(String classToLoad) {
        if (classToLoad == null) {
            return BufferMetric.UNSUPPORTED_BUFFER_METRIC;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<BufferMetric> directBufferMetricClass = (Class<BufferMetric>) Class.forName(classToLoad);
            try {
                Constructor<BufferMetric> directBufferMetricConstructor = directBufferMetricClass.getConstructor();
                return directBufferMetricConstructor.newInstance();
            } catch (NoSuchMethodException e) {
                logger.warn("Unknown BufferMetric : {}", classToLoad);
                return BufferMetric.UNSUPPORTED_BUFFER_METRIC;
            }
        } catch (Exception e) {
            logger.warn("Error creating BufferMetric [" + classToLoad + "]", e);
            return BufferMetric.UNSUPPORTED_BUFFER_METRIC;
        }
    }
}
