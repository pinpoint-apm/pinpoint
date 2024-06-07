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
import com.navercorp.pinpoint.exceptiontrace.web.entity.ClpConvertedEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionMetaDataEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionTraceSummaryEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionTraceValueViewEntity;
import com.navercorp.pinpoint.exceptiontrace.web.mapper.ExceptionMetaDataEntityMapper;
import com.navercorp.pinpoint.exceptiontrace.web.model.ClpConverted;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionTraceSummary;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionTraceValueView;
import com.navercorp.pinpoint.exceptiontrace.web.query.ClpQueryParameter;
import com.navercorp.pinpoint.exceptiontrace.web.query.ExceptionTraceQueryParameter;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionMetaDataView;
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
    private static final String SELECT_SUMMARIES_QUERY = "selectSummaries";
    private static final String SELECT_VALUEVIEWS_QUERY = "selectValueViews";
    private static final String SELECT_CLP_VARIABLES_QUERY = "selectClpVariables";

    private final SqlSessionTemplate sqlPinotSessionTemplate;

    private final ExceptionMetaDataEntityMapper mapper;

    public PinotExceptionTraceDao(
            @Qualifier("exceptionTracePinotSessionTemplate") SqlSessionTemplate sqlPinotSessionTemplate,
            ExceptionMetaDataEntityMapper mapper
    ) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public List<ExceptionMetaDataView> getExceptions(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        List<ExceptionMetaDataEntity> dataEntities = this.sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_QUERY, exceptionTraceQueryParameter);
        return dataEntities.stream()
                .map(mapper::toView)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExceptionMetaDataView> getSummarizedExceptions(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        List<ExceptionMetaDataEntity> dataEntities = this.sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_SUMMARIZED_QUERY, exceptionTraceQueryParameter);
        return dataEntities.stream()
                .map(mapper::toView)
                .collect(Collectors.toList());
    }

    @Override
    public ExceptionMetaData getException(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        ExceptionMetaDataEntity entity = this.sqlPinotSessionTemplate.selectOne(NAMESPACE + SELECT_EXACT_QUERY, exceptionTraceQueryParameter);
        return mapper.toModel(entity);
    }

    @Override
    public List<ExceptionTraceSummary> getSummaries(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        List<ExceptionTraceSummaryEntity> entities = this.sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_SUMMARIES_QUERY, exceptionTraceQueryParameter);
        return entities.stream()
                .map((ExceptionTraceSummaryEntity e) ->
                        mapper.toSummary(
                                e, exceptionTraceQueryParameter.getGroupByAttributes()
                        )
                ).collect(Collectors.toList());
    }

    @Override
    public List<ExceptionTraceValueView> getValueViews(ExceptionTraceQueryParameter exceptionTraceQueryParameter) {
        List<ExceptionTraceValueViewEntity> valueViewEntities = this.sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_VALUEVIEWS_QUERY, exceptionTraceQueryParameter);
        return valueViewEntities.stream()
                .map((ExceptionTraceValueViewEntity e) ->
                        mapper.toValueView(
                                e, exceptionTraceQueryParameter.getGroupByAttributes()
                        )
                ).collect(Collectors.toList());
    }

    @Override
    public List<ClpConverted> getReplacedVariables(ClpQueryParameter queryParameter) {
        List<ClpConvertedEntity> clpConvertedEntities = this.sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_CLP_VARIABLES_QUERY, queryParameter);

        return clpConvertedEntities.stream()
                .map(mapper::toClpConverted)
                .collect(Collectors.toList());
    }
}
