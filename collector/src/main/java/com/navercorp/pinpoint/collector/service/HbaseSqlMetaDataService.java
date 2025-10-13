/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.collector.dao.SqlMetaDataDao;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HbaseSqlMetaDataService implements SqlMetaDataService {
    private final SqlMetaDataDao sqlMetaDataDao;

    private final int maxSqlLength;

    public HbaseSqlMetaDataService(SqlMetaDataDao sqlMetaDataDao, CollectorProperties collectorProperties) {
        this.sqlMetaDataDao = Objects.requireNonNull(sqlMetaDataDao, "sqlMetaDataDao");
        Objects.requireNonNull(collectorProperties, "collectorProperties");
        this.maxSqlLength = collectorProperties.getMaxSqlLength();
    }

    @Override
    public void insert(SqlMetaDataBo sqlMetaDataBo) {
        if (sqlMetaDataBo.getSql().length() > maxSqlLength) {
            String sql = StringUtils.abbreviate(sqlMetaDataBo.getSql(), maxSqlLength);
            sqlMetaDataBo = new SqlMetaDataBo(sqlMetaDataBo.getAgentId(), sqlMetaDataBo.getAgentStartTime(), sqlMetaDataBo.getId(), sql);
        }
        this.sqlMetaDataDao.insert(sqlMetaDataBo);
    }
}
