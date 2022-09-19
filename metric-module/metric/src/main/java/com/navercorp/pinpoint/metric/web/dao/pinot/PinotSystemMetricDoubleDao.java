/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.web.dao.pinot;

import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricDao;
import com.navercorp.pinpoint.metric.web.dao.model.SystemMetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.model.SampledSystemMetric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionTemplate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricDoubleDao implements SystemMetricDao<Double> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String NAMESPACE = PinotSystemMetricDoubleDao.class.getName() + ".";

    private final SqlSessionTemplate sqlPinotSessionTemplate;

    @Value("${pinpoint.pinot.jdbc.url}")
    private String jdbcUrl;

    public PinotSystemMetricDoubleDao(SqlSessionTemplate sqlPinotSessionTemplate) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
    }

    @PostConstruct
    public void log()  {
        logger.info("#### jdbcUrl : {}", jdbcUrl);
    }

    @Override
    public List<SystemMetric> getSystemMetric(QueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectSystemMetric", queryParameter);
    }

    @Override
    public List<SampledSystemMetric<Double>> getSampledSystemMetric(QueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectSampledSystemMetric", queryParameter);
    }

    @Override
    public List<SystemMetricPoint<Double>> getSampledSystemMetricData(MetricDataSearchKey metricDataSearchKey, MetricTag metricTag) {
        logger.info("=========== thread start " + Thread.currentThread().getName() + " metrictagInfo : ");
        long startTime = System.currentTimeMillis();
        SystemMetricDataSearchKey systemMetricDataSearchKey = new SystemMetricDataSearchKey(metricDataSearchKey, metricTag);


        List<SystemMetricPoint<Double>> result = sqlPinotSessionTemplate.selectList(NAMESPACE + "selectSampledSystemMetricData", systemMetricDataSearchKey);
        long endTime = System.currentTimeMillis();
        logger.info("============ thread end " + Thread.currentThread().getName() + " end thread." + " executeTime : " + (endTime - startTime) + " metrictagInfo : " + metricTag);
        return result;
    }
}
