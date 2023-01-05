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

import com.navercorp.pinpoint.uristat.common.model.UriStat;
import com.navercorp.pinpoint.uristat.web.dao.UriStatDao;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.util.UriStatQueryParameter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UriStatServiceImpl implements UriStatService {

    private final UriStatDao uriStatDao;

    public UriStatServiceImpl(UriStatDao uriStatDao) {
        this.uriStatDao = Objects.requireNonNull(uriStatDao);
    }

    @Override
    public List<UriStat> getCollectedUriStatApplication(UriStatQueryParameter queryParameter) {
        return uriStatDao.getUriStatApplication(queryParameter);
    }

    @Override
    public List<UriStat> getCollectedUriStatAgent(UriStatQueryParameter queryParameter) {
        return uriStatDao.getUriStatAgent(queryParameter);
    }

    @Override
    public List<UriStatSummary> getUriStatApplicationSummary(UriStatQueryParameter queryParameter) {
        return uriStatDao.getUriStatApplicationSummary(queryParameter);
    }

    @Override
    public List<UriStatSummary> getUriStatAgentSummary(UriStatQueryParameter queryParameter) {
        return uriStatDao.getUriStatAgentSummary(queryParameter);
    }
}
