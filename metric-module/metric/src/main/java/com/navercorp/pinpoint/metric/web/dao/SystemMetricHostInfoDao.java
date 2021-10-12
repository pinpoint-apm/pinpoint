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

package com.navercorp.pinpoint.metric.web.dao;

import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;

import java.util.List;

/**
 * @author minwoo.jung
 */
public interface SystemMetricHostInfoDao {

    List<String> selectHostGroupNameList();

    List<String> selectHostList(String hostGroupName);

    List<String> getCollectedMetricInfo(String hostGroupName, String hostName);

    MetricTagCollection selectMetricTagCollection(MetricTagKey metricTagKey);
}
