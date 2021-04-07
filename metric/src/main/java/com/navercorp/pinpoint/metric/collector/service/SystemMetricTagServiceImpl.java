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

import com.navercorp.pinpoint.metric.collector.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
public class SystemMetricTagServiceImpl implements SystemMetricTagService {

    private final MetricTagCache metricTagCache;

    public SystemMetricTagServiceImpl(MetricTagCache metricTagCache) {
        this.metricTagCache = Objects.requireNonNull(metricTagCache, "metricTagCache");
    }

    @Override
    public void saveMetricTag(String applicationName, SystemMetric systemMetric) {
        final String metricName = systemMetric.getMetricName();
        final String fieldName = systemMetric.getFieldName();
        final List<Tag> tagList = systemMetric.getTags();
        MetricTagCollection metricTagCollection = metricTagCache.getMetricTag(applicationName, systemMetric.getMetricName(), systemMetric.getFieldName());

        if (Objects.isNull(metricTagCollection)) {
            List<Tag> copiedTagList = tagListCopy(tagList);
            metricTagCache.saveMetricTag(new MetricTag(applicationName, metricName, fieldName, copiedTagList));
            metricTagCache.updateCacheforMetricTag(createMetricTagCollection(applicationName, metricName, fieldName, copiedTagList));
        } else {
            for (MetricTag metricTag : metricTagCollection.getMetricTagList()) {
                if(isEquals(tagList, metricTag.getTags())) {
                    return;
                }
            }

            List<Tag> copiedTagList = tagListCopy(tagList);
            metricTagCache.saveMetricTag(new MetricTag(applicationName, metricName, fieldName, copiedTagList));
            metricTagCache.updateCacheforMetricTag(createMetricTagCollection(metricTagCollection, copiedTagList));

        }
    }

    private MetricTagCollection createMetricTagCollection(String applicationName, String metricName, String fieldName, List<Tag> tagList) {
        MetricTag metricTag = new MetricTag(applicationName, metricName, fieldName, tagList);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);

        return new MetricTagCollection(applicationName, metricName, fieldName, metricTagList);
    }

    private MetricTagCollection createMetricTagCollection(MetricTagCollection metricTagCollection, List<Tag> tagList) {
        List<MetricTag> metricTagList = new ArrayList<>(metricTagCollection.getMetricTagList().size());

        for (MetricTag metricTag : metricTagCollection.getMetricTagList()) {
            metricTagList.add(metricTag.copy());
        }

        metricTagList.add(new MetricTag(metricTagCollection.getApplicationId(), metricTagCollection.getMetricName(),metricTagCollection.getFieldName(), tagList));

        return new MetricTagCollection(metricTagCollection.getApplicationId(), metricTagCollection.getMetricName(), metricTagCollection.getFieldName(), metricTagList);
    }

    private List<Tag> tagListCopy(List<Tag> tags) {
        List<Tag> tagList = new ArrayList<Tag>(tags.size());

        for (Tag tag : tagList) {
            tagList.add(tag.copy());
        }

        return tagList;
    }

    protected boolean isEquals(List<Tag> tagList1, List<Tag> tagList2) {
        if (tagList1.size() != tagList2.size()) {
            return false;
        }
        if(tagList1.containsAll(tagList2) == false) {
            return false;
        }

        return true;
    }
}
