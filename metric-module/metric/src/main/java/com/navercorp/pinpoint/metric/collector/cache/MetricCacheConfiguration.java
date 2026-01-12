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

package com.navercorp.pinpoint.metric.collector.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.common.server.config.CommonCacheManagerConfiguration;
import com.navercorp.pinpoint.metric.collector.config.MetricCollectorProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

/**
 * @author minwoo.jung
 */
@Configuration
@EnableCaching
@Import(CommonCacheManagerConfiguration.class)
public class MetricCacheConfiguration {

    public static final String METRIC_TAG_COLLECTION_CACHE_NAME = "metricTagCollection";
    public static final String METRIC_DATA_TYPE_CACHE_NAME = "metricDataType";

    @Bean
    public CacheManager metricTagCollection(MetricCollectorProperties metricCollectorProperties) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(METRIC_TAG_COLLECTION_CACHE_NAME);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(86400, TimeUnit.SECONDS)
                .initialCapacity(metricCollectorProperties.getMetricTagCacheInitSize())
                .maximumSize(metricCollectorProperties.getMetricTagCacheSize()));
        return caffeineCacheManager;
    }

    @Bean
    public CacheManager metricDataType() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(METRIC_DATA_TYPE_CACHE_NAME);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(604800, TimeUnit.SECONDS)
                .initialCapacity(500)
                .maximumSize(1000));
        return caffeineCacheManager;
    }


}
