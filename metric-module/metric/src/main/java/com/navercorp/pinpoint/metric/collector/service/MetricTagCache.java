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

package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.collector.cache.MetricCacheConfiguration;
import com.navercorp.pinpoint.metric.collector.dao.MetricTagDao;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * @author minwoo.jung
 */
@Component
public class MetricTagCache {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final MetricTagDao metricTagDao;

    public MetricTagCache(MetricTagDao metricTagDao) {
        this.metricTagDao = Objects.requireNonNull(metricTagDao, "metricTagDao");
    }

    @Cacheable(cacheNames = "metricTagCollection", key = "#metricTagKey", cacheManager = MetricCacheConfiguration.METRIC_TAG_COLLECTION_CACHE_NAME)
    public MetricTagCollection getMetricTag(MetricTagKey metricTagKey) {
        MetricTagCollection metricTagCollection = metricTagDao.selectMetricTag(metricTagKey);

        if (logger.isDebugEnabled()) {
            logger.debug("metricTagCollection metricTagKey: {}, metricTagCollection : {}", metricTagKey, metricTagCollection);
        }

        return metricTagCollection;
    }

    public void saveMetricTag(MetricTag metricTag) {
        metricTagDao.insertMetricTag(metricTag);
    }

    @CachePut(cacheNames = "metricTagCollection", key = "#metricTagKey", cacheManager = MetricCacheConfiguration.METRIC_TAG_COLLECTION_CACHE_NAME)
    public MetricTagCollection updateCacheForMetricTag(MetricTagKey metricTagKey, MetricTagCollection metricTagCollection) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateCacheForMetricTag metricTagKey: {}, metricTagCollection : {}", metricTagKey, metricTagCollection);
        }

        return metricTagCollection;
    }
}
