/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.ApiMetaDataDao;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ApiMetaDataService {

    private final ApiMetaDataDao sqlMetaDataDao;

    public ApiMetaDataService(ApiMetaDataDao sqlMetaDataDao) {
        this.sqlMetaDataDao = Objects.requireNonNull(sqlMetaDataDao, "sqlMetaDataDao");
    }

    public void insert(final ApiMetaDataBo apiMetaDataBo) {
        sqlMetaDataDao.insert(apiMetaDataBo);
    }
}