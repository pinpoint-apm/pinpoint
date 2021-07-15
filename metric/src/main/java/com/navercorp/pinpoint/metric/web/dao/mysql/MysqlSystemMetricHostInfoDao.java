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

package com.navercorp.pinpoint.metric.web.dao.mysql;

import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostInfoDao;
import com.navercorp.pinpoint.metric.web.dao.model.MetricInfoSearchKey;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlSystemMetricHostInfoDao implements SystemMetricHostInfoDao {

    private static final String NAMESPACE = SystemMetricHostInfoDao.class.getPackage().getName() + "." + SystemMetricHostInfoDao.class.getSimpleName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlSystemMetricHostInfoDao(@Qualifier("metricMysqlSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "metricMysqlSqlSessionTemplate");
    }

    @Override
    public List<String> selectHostGroupIdList() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectHostGroupIdList");
    }

    @Override
    public List<String> selectHostList(String hostGroupId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectHostList", hostGroupId);
    }

    @Override
    public List<String> getCollectedMetricInfo(String hostGroupId, String hostName) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectCollectedMetricInfo", new MetricInfoSearchKey(hostGroupId, hostName));
    }

    @Override
    public MetricTagCollection selectMetricTagCollection(MetricTagKey metricTagKey) {
        List<MetricTag> metricTagList = sqlSessionTemplate.selectList(NAMESPACE + "selectMetricTagList", metricTagKey);
        return new MetricTagCollection(metricTagKey.getHostGroupId(), metricTagKey.getHostName(), metricTagKey.getMetricName(), metricTagKey.getFieldName(), metricTagList);
    }


}
