/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.uristat.web.dao;

import com.navercorp.pinpoint.uristat.web.entity.UriStatSummaryEntity;
import com.navercorp.pinpoint.uristat.web.mapper.EntityToModelMapper;
import com.navercorp.pinpoint.uristat.web.mapper.MapperUtils;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.util.UriStatSummaryQueryParameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author intr3p1d
 */
@Repository
public class PinotFailureSummaryDao implements UriStatSummaryDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String NAMESPACE = PinotUriStatSummaryDao.class.getName() + ".";
    private static final String URI_STAT_SUMMARY_FAILURE = "uriStatSummaryFailure";
    private final SqlSessionTemplate sqlPinotSessionTemplate;
    private final EntityToModelMapper mapper;

    public PinotFailureSummaryDao(
            @Qualifier("uriStatPinotSessionTemplate") SqlSessionTemplate sqlPinotSessionTemplate,
            EntityToModelMapper mapper
    ) {
        this.sqlPinotSessionTemplate = sqlPinotSessionTemplate;
        this.mapper = mapper;
    }

    @Override
    public List<UriStatSummary> getUriStatPagedSummary(UriStatSummaryQueryParameter uriStatQueryParameter) {
        List<UriStatSummaryEntity> entities = sqlPinotSessionTemplate.selectList(
                NAMESPACE + URI_STAT_SUMMARY_FAILURE, uriStatQueryParameter
        );
        List<List<UriStatSummaryEntity>> listOfEntities = MapperUtils.groupByUriAndVersion(entities, uriStatQueryParameter.getLimit());
        return listOfEntities.stream()
                .map(mapper::toFailureSummary
                ).toList();
    }
}
