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

package com.navercorp.pinpoint.profiler.context.provider.stat.buffer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * @author Roy Kim
 */
public class BufferMetricProvider implements Provider<BufferMetric> {

    private static final String BUFFER_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.buffer.DefaultBufferMetric";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public BufferMetricProvider() {
    }

    @Override
    public BufferMetric get() {

        final JvmVersion jvmVersion = JvmUtils.getVersion();
        if (!jvmVersion.onOrAfter(JvmVersion.JAVA_7)) {
            logger.debug("Unsupported JVM version. {}", jvmVersion);
            return BufferMetric.UNSUPPORTED_BUFFER_METRIC;
        }

        BufferMetric bufferMetric = createBufferMetric(BUFFER_METRIC);
        logger.info("loaded : {}", bufferMetric);
        return bufferMetric;
    }

    private BufferMetric createBufferMetric(String classToLoad) {
        if (classToLoad == null) {
            return BufferMetric.UNSUPPORTED_BUFFER_METRIC;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<BufferMetric> bufferMetricClass = (Class<BufferMetric>) Class.forName(classToLoad);
            Constructor<BufferMetric> bufferMetricConstructor = bufferMetricClass.getConstructor();
            return bufferMetricConstructor.newInstance();
        } catch (Exception e) {
            logger.warn("BufferMetric initialize fail: {}", classToLoad, e);
            return BufferMetric.UNSUPPORTED_BUFFER_METRIC;
        }
    }
}
