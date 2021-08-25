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

package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostInfoDao;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.ElementOfBasicGroup;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.MatchingRule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
public class SystemMetricHostInfoServiceImpl implements SystemMetricHostInfoService {

    private final SystemMetricHostInfoDao systemMetricHostInfoDao;
    private final SystemMetricBasicGroupManager systemMetricBasicGroupManager;

    public SystemMetricHostInfoServiceImpl(SystemMetricHostInfoDao systemMetricHostInfoDao, SystemMetricBasicGroupManager systemMetricBasicGroupManager) {
        this.systemMetricHostInfoDao = systemMetricHostInfoDao;
        this.systemMetricBasicGroupManager = Objects.requireNonNull(systemMetricBasicGroupManager, "systemMetricBasicGroupManager");
    }

    @Override
    public List<String> getHostGroupIdList() {
        return systemMetricHostInfoDao.selectHostGroupIdList();
    }

    @Override
    public List<String> getHostList(String hostGroupId) {
        return systemMetricHostInfoDao.selectHostList(hostGroupId);
    }

    @Override
    public List<String> getCollectedMetricInfo(String hostGroupId, String hostName) {
        List<String> metricNameList = systemMetricHostInfoDao.getCollectedMetricInfo(hostGroupId, hostName);

        List<String> metricDefinitionIdList = new LinkedList<String>();
        for (String metricName : metricNameList) {
            metricDefinitionIdList.addAll(systemMetricBasicGroupManager.findMetricDefinitionIdList(metricName));
        }

        return metricDefinitionIdList;
    }

    @Override
    public List<MetricTag> getTag(MetricDataSearchKey metricDataSearchKey, ElementOfBasicGroup elementOfBasicGroup) {
        MetricTagCollection metricTagCollection = systemMetricHostInfoDao.selectMetricTagCollection(new MetricTagKey(metricDataSearchKey.getHostGroupId(), metricDataSearchKey.getHostName(), metricDataSearchKey.getMetricName() , elementOfBasicGroup.getFieldName()));

        MatchingRule matchingRule = elementOfBasicGroup.getMatchingRule();

        switch (matchingRule) {
            case EXACT :
                return exactMatchingTag(metricTagCollection, metricDataSearchKey, elementOfBasicGroup);
            default :
                throw new UnsupportedOperationException("unsupported matchingRule:" + matchingRule);
        }
    }

    private List<MetricTag> exactMatchingTag(MetricTagCollection metricTagCollection, MetricDataSearchKey metricDataSearchKey, ElementOfBasicGroup elementOfBasicGroup) {
        List<MetricTag> metricTagList = metricTagCollection.getMetricTagList();
        List<Tag> tagList = elementOfBasicGroup.getTagList();
        List<MetricTag> exactMetricTagList = new ArrayList<>();

        for (MetricTag metricTag : metricTagList) {
            List<Tag> collectedTagList = metricTag.getTags();
            if (tagList.size() != collectedTagList.size()) {
                continue;
            }
            if (collectedTagList.containsAll(tagList)) {
                exactMetricTagList.add(metricTag);
            }
        }

        return exactMetricTagList;
    }
}
