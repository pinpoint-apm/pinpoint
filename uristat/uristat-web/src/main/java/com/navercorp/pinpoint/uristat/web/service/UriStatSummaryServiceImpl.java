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

package com.navercorp.pinpoint.uristat.web.service;

import com.navercorp.pinpoint.uristat.web.dao.UriStatSummaryDao;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.util.UriStatSummaryQueryParameter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UriStatSummaryServiceImpl implements UriStatSummaryService {

    private final UriStatSummaryDao uriStatSummaryDao;

    public UriStatSummaryServiceImpl(UriStatSummaryDao uriStatDao) {
        this.uriStatSummaryDao = Objects.requireNonNull(uriStatDao);
    }

    @Override
    @Deprecated
    public List<UriStatSummary> getUriStatApplicationSummary(UriStatSummaryQueryParameter queryParameter) {
        return uriStatSummaryDao.getUriStatApplicationSummary(queryParameter);
    }

    @Override
    @Deprecated
    public List<UriStatSummary> getUriStatAgentSummary(UriStatSummaryQueryParameter queryParameter) {
        return uriStatSummaryDao.getUriStatAgentSummary(queryParameter);
    }

    @Override
    public List<UriStatSummary> getUriStatApplicationPagedSummary(UriStatSummaryQueryParameter queryParameter) {
        return uriStatSummaryDao.getUriStatApplicationPagedSummary(queryParameter);
    }

    @Override
    public List<UriStatSummary> getUriStatAgentPagedSummary(UriStatSummaryQueryParameter queryParameter) {
        return uriStatSummaryDao.getUriStatAgentPagedSummary(queryParameter);
    }

}
