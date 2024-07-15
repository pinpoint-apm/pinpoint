/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.exceptiontrace.web.dao;

import com.navercorp.pinpoint.exceptiontrace.common.model.ExceptionMetaData;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ErrorSummaryEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionGroupSummaryEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionMetaDataEntity;
import com.navercorp.pinpoint.exceptiontrace.web.model.ErrorSummary;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionChartValueViewEntity;
import com.navercorp.pinpoint.exceptiontrace.web.mapper.ExceptionEntityMapper;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionGroupSummary;
import com.navercorp.pinpoint.exceptiontrace.web.util.ExceptionTraceQueryParameter;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartValueView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionDetailView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author intr3p1d
 */
@Repository
public class PinotExceptionTraceDao implements ExceptionTraceDao {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String NAMESPACE = PinotExceptionTraceDao.class.getName() + ".";

    private static final String SELECT_QUERY = "selectExceptions";
    private static final String SELECT_SUMMARIZED_QUERY = "selectSummarizedExceptions";
    private static final String SELECT_EXACT_QUERY = "selectExactException";
    private static final String SELECT_GROUP_SUMMARIES_QUERY = "selectGroupSummaries";
    private static final String SELECT_CHART_QUERY = "selectChartValueViews";
    private static final String SELECT_ERROR_SUMMARIES_QUERY = "selectErrorSummaries";

    private final SqlSessionTemplate sqlPinotSessionTemplate;

    private final ExceptionEntityMapper mapper;

    public PinotExceptionTraceDao(
            @Qualifier("exceptionTracePinotSessionTemplate") SqlSessionTemplate sqlPinotSessionTemplate,
            ExceptionEntityMapper mapper
    ) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public List<ExceptionDetailView> getExceptions(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        List<ExceptionMetaDataEntity> dataEntities = this.sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_QUERY, exceptionTraceQueryParameter);
        return dataEntities.stream()
                .map(mapper::toDetailView)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExceptionDetailView> getSummarizedExceptions(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        List<ExceptionMetaDataEntity> dataEntities = this.sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_SUMMARIZED_QUERY, exceptionTraceQueryParameter);
        return dataEntities.stream()
                .map(mapper::toDetailView)
                .collect(Collectors.toList());
    }

    @Override
    public ExceptionMetaData getException(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        ExceptionMetaDataEntity entity = this.sqlPinotSessionTemplate.selectOne(NAMESPACE + SELECT_EXACT_QUERY, exceptionTraceQueryParameter);
        return mapper.toModel(entity);
    }

    @Override
    public List<ExceptionGroupSummary> getGroupSummaries(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        List<ExceptionGroupSummaryEntity> entities = this.sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_GROUP_SUMMARIES_QUERY, exceptionTraceQueryParameter);
        return entities.stream()
                .map((ExceptionGroupSummaryEntity e) ->
                        mapper.toSummary(
                                e, exceptionTraceQueryParameter.getGroupByAttributes()
                        )
                ).collect(Collectors.toList());
    }

    @Override
    public List<ExceptionChartValueView> getChartValueViews(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        List<ExceptionChartValueViewEntity> valueViewEntities = this.sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_CHART_QUERY, exceptionTraceQueryParameter);
        for (int i = 0; i < valueViewEntities.size(); i++) {
            ExceptionChartValueViewEntity view = valueViewEntities.get(i);
            view.setRowNum(i + 1);
        }

        return valueViewEntities.stream()
                .map((ExceptionChartValueViewEntity e) ->
                        mapper.toChartView(
                                e, exceptionTraceQueryParameter.getGroupByAttributes()
                        )
                ).collect(Collectors.toList());
    }

    @Override
    public List<ErrorSummary> getErrorSummaries(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        List<ErrorSummaryEntity> entities = this.sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_ERROR_SUMMARIES_QUERY, exceptionTraceQueryParameter);
        return entities.stream()
                .map((ErrorSummaryEntity e) ->
                        mapper.toErrorSummary(
                                e, exceptionTraceQueryParameter.getGroupByAttributes()
                        )
                ).collect(Collectors.toList());
    }
}
