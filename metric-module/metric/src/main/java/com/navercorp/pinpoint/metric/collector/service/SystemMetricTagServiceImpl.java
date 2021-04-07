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

import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
public class SystemMetricTagServiceImpl implements SystemMetricTagService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final MetricTagCache metricTagCache;

    public SystemMetricTagServiceImpl(MetricTagCache metricTagCache) {
        this.metricTagCache = Objects.requireNonNull(metricTagCache, "metricTagCache");
    }

    @Override
    public void saveMetricTag(String applicationName, SystemMetric systemMetric) {
        final String metricName = systemMetric.getMetricName();
        final String hostName = systemMetric.getHostName();
        final String fieldName = systemMetric.getFieldName();
        final List<Tag> tagList = systemMetric.getTags();

        MetricTagCollection metricTagCollection = metricTagCache.getMetricTag(new MetricTagKey(applicationName, hostName, metricName, fieldName));

        if (Objects.isNull(metricTagCollection)) {
            MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName);
            List<Tag> copiedTagList = tagListCopy(tagList);

            metricTagCache.updateCacheForMetricTag(metricTagKey, createMetricTagCollection(applicationName, hostName, metricName, fieldName, copiedTagList));
            metricTagCache.saveMetricTag(new MetricTag(applicationName, hostName, metricName, fieldName, copiedTagList));
        } else {
            for (MetricTag metricTag : metricTagCollection.getMetricTagList()) {
                if (isEquals(tagList, metricTag.getTags())) {
                    return;
                }
            }
            MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName);
            List<Tag> copiedTagList = tagListCopy(tagList);

            metricTagCache.updateCacheForMetricTag(metricTagKey, createMetricTagCollection(metricTagCollection, copiedTagList));
            metricTagCache.saveMetricTag(new MetricTag(applicationName, hostName, metricName, fieldName, copiedTagList));
        }
    }

    MetricTagCollection createMetricTagCollection(String applicationName, String hostName, String metricName, String fieldName, List<Tag> tagList) {
        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, tagList);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);

        return new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);
    }

    MetricTagCollection createMetricTagCollection(MetricTagCollection metricTagCollection, List<Tag> tagList) {
        List<MetricTag> metricTagList = new ArrayList<>(metricTagCollection.getMetricTagList().size());

        for (MetricTag metricTag : metricTagCollection.getMetricTagList()) {
            metricTagList.add(metricTag.copy());
        }

        metricTagList.add(new MetricTag(metricTagCollection.getHostGroupName(), metricTagCollection.getHostName(), metricTagCollection.getMetricName(),metricTagCollection.getFieldName(), tagList));

        return new MetricTagCollection(metricTagCollection.getHostGroupName(), metricTagCollection.getHostName(), metricTagCollection.getMetricName(), metricTagCollection.getFieldName(), metricTagList);
    }

    List<Tag> tagListCopy(List<Tag> tags) {
        List<Tag> tagList = new ArrayList<>(tags.size());

        for (Tag tag : tags) {
            tagList.add(tag.copy());
        }

        return tagList;
    }

    boolean isEquals(List<Tag> tagList1, List<Tag> tagList2) {
        if (tagList1.containsAll(tagList2) == false) {
            return false;
        }

        return true;
    }
}
