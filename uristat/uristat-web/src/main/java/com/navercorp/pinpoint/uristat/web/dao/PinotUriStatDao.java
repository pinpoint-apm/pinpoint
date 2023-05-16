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

package com.navercorp.pinpoint.uristat.web.dao;

import com.navercorp.pinpoint.uristat.web.model.UriStatHistogram;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.util.UriStatQueryParameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class PinotUriStatDao implements UriStatDao {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String NAMESPACE = PinotUriStatDao.class.getName() + ".";

    private final SqlSessionTemplate sqlPinotSessionTemplate;

    public PinotUriStatDao(@Qualifier("uriStatPinotSessionTemplate") SqlSessionTemplate sqlPinotSessionTemplate) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
    }

    @Override
    public List<UriStatHistogram> getUriStatApplication(UriStatQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectTotalUriStatApplication", queryParameter);
    }

    @Override
    public List<UriStatHistogram> getUriStatAgent(UriStatQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectTotalUriStatAgentId", queryParameter);
    }

    @Override
    public List<UriStatHistogram> getFailedUriStatApplication(UriStatQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectFailedUriStatApplication", queryParameter);
    }

    @Override
    public List<UriStatHistogram> getFailedUriStatAgent(UriStatQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectFailedUriStatAgentId", queryParameter);
    }

    @Override
    public List<UriStatSummary> getUriStatApplicationPagedSummary(UriStatQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "uriStatApplicationSummary", queryParameter);
    }

    @Override
    public List<UriStatSummary> getUriStatAgentPagedSummary(UriStatQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "uriStatAgentSummary", queryParameter);
    }

    @Override
    @Deprecated
    public List<UriStatSummary> getUriStatApplicationSummary(UriStatQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "top50UriStatApplication", queryParameter);
    }

    @Override
    @Deprecated
    public List<UriStatSummary> getUriStatAgentSummary(UriStatQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "top50UriStatAgent", queryParameter);
    }

}
