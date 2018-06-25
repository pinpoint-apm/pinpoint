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

package com.navercorp.pinpoint.profiler.context.provider.stat.directbuffer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.monitor.metric.directbuffer.DirectBufferMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * @author Roy Kim
 */
public class DirectBufferMetricProvider implements Provider<DirectBufferMetric> {

    private static final String DIRECT_BUFFER_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.directbuffer.DefaultDirectBufferMetric";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DirectBufferMetricProvider() {
    }

    @Override
    public DirectBufferMetric get() {

        String classToLoad = null;
        JvmVersion jvmVersion = JvmUtils.getVersion();

        if (jvmVersion.onOrAfter(JvmVersion.JAVA_7)) {
            classToLoad = DIRECT_BUFFER_METRIC;
        }else{
            logger.warn("Unsupported JVM version.");
            return DirectBufferMetric.UNSUPPORTED_DIRECT_BUFFER_METRIC;
        }

        DirectBufferMetric directBufferMetric = createDirectBufferMetric(classToLoad);
        logger.info("loaded : {}", directBufferMetric);
        return directBufferMetric;
    }

    private DirectBufferMetric createDirectBufferMetric(String classToLoad) {
        if (classToLoad == null) {
            return DirectBufferMetric.UNSUPPORTED_DIRECT_BUFFER_METRIC;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<DirectBufferMetric> directBufferMetricClass = (Class<DirectBufferMetric>) Class.forName(classToLoad);
            try {
                Constructor<DirectBufferMetric> directBufferMetricConstructor = directBufferMetricClass.getConstructor();
                return directBufferMetricConstructor.newInstance();
            } catch (NoSuchMethodException e) {
                logger.warn("Unknown DirectBufferMetric : {}", classToLoad);
                return DirectBufferMetric.UNSUPPORTED_DIRECT_BUFFER_METRIC;
            }
        } catch (Exception e) {
            logger.warn("Error creating DirectBufferMetric [" + classToLoad + "]", e);
            return DirectBufferMetric.UNSUPPORTED_DIRECT_BUFFER_METRIC;
        }
    }
}
