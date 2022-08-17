/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.dao.pinot;

import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostInfoDao;
import com.navercorp.pinpoint.metric.web.dao.model.MetricInfoSearchKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class PinotSystemMetricHostInfoDao implements SystemMetricHostInfoDao {

    private static final String NAMESPACE = PinotSystemMetricHostInfoDao.class.getName() + ".";

    private final SqlSessionTemplate sqlPinotSessionTemplate;

    public PinotSystemMetricHostInfoDao(SqlSessionTemplate sqlPinotSessionTemplate) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
    }

    @Override
    public List<String> selectHostGroupNameList() {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectHostGroupNameList");
    }

    @Override
    public List<String> selectHostList(String hostGroupName) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectHostList", hostGroupName);
    }

    @Override
    public List<String> getCollectedMetricInfo(String hostGroupName, String hostName) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectCollectedMetricInfo", new MetricInfoSearchKey(hostGroupName, hostName));
    }

    @Override
    public MetricTagCollection selectMetricTagCollection(MetricTagKey metricTagKey) {
        List<MetricTag> metricTagList = sqlPinotSessionTemplate.selectList(NAMESPACE + "selectMetricTagList", metricTagKey);
        return new MetricTagCollection(metricTagKey.getHostGroupName(), metricTagKey.getHostName(), metricTagKey.getMetricName(), metricTagKey.getFieldName(), metricTagList);
    }
}
