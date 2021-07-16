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

import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricDataTypeDao;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlSystemMetricDataTypeDao implements SystemMetricDataTypeDao {

    private static final String NAMESPACE = SystemMetricDataTypeDao.class.getName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlSystemMetricDataTypeDao(@Qualifier("metricMysqlSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "metricMysqlSqlSessionTemplate");
    }

    @Override
    public MetricData selectMetricDataType(MetricDataName metricDataName) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectMetricDataType", metricDataName);
    }
}
