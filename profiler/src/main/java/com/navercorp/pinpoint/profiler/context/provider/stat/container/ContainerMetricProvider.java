/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.container;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.ContainerResolver;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.monitor.metric.container.ContainerMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Constructor;
import static com.navercorp.pinpoint.common.util.JvmVersion.JAVA_8;

/**
 * @author Hyunjoon Cho
 */
public class ContainerMetricProvider implements Provider<ContainerMetric> {

    private static final String CONTAINER_METRIC = "com.navercorp.pinpoint.profiler.monitor.metric.container.DefaultContainerMetric";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public ContainerMetricProvider(){
    }

    @Override
    public ContainerMetric get() {

        final JvmVersion jvmVersion = JvmUtils.getVersion();
        if (!jvmVersion.onOrAfter(JAVA_8)) {
            logger.debug("Unsupported JVM version. {}", jvmVersion);
            return ContainerMetric.UNSUPPORTED_CONTAINER_METRIC;
        }

        ContainerResolver containerResolver = new ContainerResolver();
        if (!containerResolver.isContainer()){
            logger.debug("Not a container");
            return ContainerMetric.UNSUPPORTED_CONTAINER_METRIC;
        }

        ContainerMetric containerMetric = createContainerMetric(CONTAINER_METRIC);
        logger.info("loaded : {}", containerMetric);
        return containerMetric;
    }

    private ContainerMetric createContainerMetric(String classToLoad) {
        if (classToLoad == null) {
            return ContainerMetric.UNSUPPORTED_CONTAINER_METRIC;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<ContainerMetric> containerMetricClass = (Class<ContainerMetric>) Class.forName(classToLoad);
            Constructor<ContainerMetric> containerMetricConstructor = containerMetricClass.getConstructor();
            return containerMetricConstructor.newInstance();
        } catch (Exception e) {
            logger.info("ContainerMetric initialize fail: {}", classToLoad, e);
            return ContainerMetric.UNSUPPORTED_CONTAINER_METRIC;
        }
    }
}
