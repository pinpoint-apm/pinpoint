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

import com.navercorp.pinpoint.common.server.util.time.DateTimeUtils;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
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
    public void saveMetricTag(String tenantId, String hostGroupName, SystemMetric systemMetric) {
        final String metricName = systemMetric.getMetricName();
        final String hostName = systemMetric.getHostName();
        final String fieldName = systemMetric.getFieldName();
        final List<Tag> tagList = systemMetric.getTags();
        final long saveTime = getSaveTime();

        MetricTagCollection metricTagCollection = metricTagCache.getMetricTag(new MetricTagKey(tenantId, hostGroupName, hostName, metricName, fieldName, saveTime));//

        if (metricTagCollection == null) {
            MetricTagKey metricTagKey = new MetricTagKey(tenantId, hostGroupName, hostName, metricName, fieldName, saveTime);
            List<Tag> copiedTagList = tagListCopy(tagList);

            metricTagCache.updateCacheForMetricTag(metricTagKey, createMetricTagCollection(tenantId, hostGroupName, hostName, metricName, fieldName, copiedTagList, saveTime));
            metricTagCache.saveMetricTag(new MetricTag(tenantId, hostGroupName, hostName, metricName, fieldName, copiedTagList, saveTime));
        } else {
            for (MetricTag metricTag : metricTagCollection.getMetricTagList()) {
                if (isEquals(tagList, metricTag.getTags())) {
                    return;
                }
            }
            MetricTagKey metricTagKey = new MetricTagKey(tenantId, hostGroupName, hostName, metricName, fieldName, saveTime);
            List<Tag> copiedTagList = tagListCopy(tagList);

            metricTagCache.updateCacheForMetricTag(metricTagKey, createMetricTagCollection(metricTagCollection, copiedTagList, saveTime));
            metricTagCache.saveMetricTag(new MetricTag(tenantId, hostGroupName, hostName, metricName, fieldName, copiedTagList, saveTime));
        }
    }

    MetricTagCollection createMetricTagCollection(String tenantId, String applicationName, String hostName, String metricName, String fieldName, List<Tag> tagList, long saveTime) {
        MetricTag metricTag = new MetricTag(tenantId, applicationName, hostName, metricName, fieldName, tagList, saveTime);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);

        return new MetricTagCollection(tenantId, applicationName, hostName, metricName, fieldName, metricTagList);
    }

    MetricTagCollection createMetricTagCollection(MetricTagCollection metricTagCollection, List<Tag> tagList, long saveTime) {
        List<MetricTag> metricTagList = new ArrayList<>(metricTagCollection.getMetricTagList().size());

        for (MetricTag metricTag : metricTagCollection.getMetricTagList()) {
            metricTagList.add(metricTag.copy());
        }

        metricTagList.add(new MetricTag(metricTagCollection.getTenantId(), metricTagCollection.getHostGroupName(), metricTagCollection.getHostName(), metricTagCollection.getMetricName(),metricTagCollection.getFieldName(), tagList, saveTime));

        return new MetricTagCollection(metricTagCollection.getTenantId(), metricTagCollection.getHostGroupName(), metricTagCollection.getHostName(), metricTagCollection.getMetricName(), metricTagCollection.getFieldName(), metricTagList);
    }

    List<Tag> tagListCopy(List<Tag> tags) {
        return new ArrayList<>(tags);
    }

    boolean isEquals(List<Tag> tagList1, List<Tag> tagList2) {
        if (tagList1.size() != tagList2.size()) {
            return false;
        }

        if (tagList1.containsAll(tagList2) == false) {
            return false;
        }

        return true;
    }

    private long getSaveTime() {
        return DateTimeUtils.previousOrSameSundayToMillis();
    }
}
