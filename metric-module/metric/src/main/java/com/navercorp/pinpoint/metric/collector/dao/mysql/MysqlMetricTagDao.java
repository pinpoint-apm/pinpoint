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

package com.navercorp.pinpoint.metric.collector.dao.mysql;

import com.navercorp.pinpoint.metric.collector.dao.MetricTagDao;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlMetricTagDao implements MetricTagDao {

    private static final String NAMESPACE = MetricTagDao.class.getName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlMetricTagDao(@Qualifier("metricSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "metricSqlSessionTemplate");
    }

    @Override
    public void insertMetricTag(MetricTag metricTag) {
        sqlSessionTemplate.insert(NAMESPACE + "insertMetricTag", metricTag);
    }

    @Override
    public MetricTagCollection selectMetricTag(MetricTagKey metricTagKey) {
        List<MetricTag> metricTagList = sqlSessionTemplate.selectList(NAMESPACE + "selectMetricTagList", metricTagKey);
        return new MetricTagCollection(metricTagKey.getHostGroupName(), metricTagKey.getHostName(), metricTagKey.getMetricName(), metricTagKey.getFieldName(), metricTagList);
    }
}
