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
import com.navercorp.pinpoint.collector.dao.SqlUidMetaDataDao;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HbaseSqlUidMetaDataService implements SqlUidMetaDataService {
    private final SqlUidMetaDataDao sqlUidMetaDataDao;

    private final int maxSqlLength;

    public HbaseSqlUidMetaDataService(SqlUidMetaDataDao sqlUidMetaDataDao, CollectorProperties collectorProperties) {
        this.sqlUidMetaDataDao = Objects.requireNonNull(sqlUidMetaDataDao, "sqlUidMetaDataDao");
        Objects.requireNonNull(collectorProperties, "collectorProperties");
        this.maxSqlLength = collectorProperties.getMaxSqlLength();
    }

    @Override
    public void insert(SqlUidMetaDataBo sqlUidMetaDataBo) {
        if (sqlUidMetaDataBo.getSql().length() > maxSqlLength) {
            String sql = StringUtils.abbreviate(sqlUidMetaDataBo.getSql(), maxSqlLength);
            sqlUidMetaDataBo = new SqlUidMetaDataBo(sqlUidMetaDataBo.getAgentId(), sqlUidMetaDataBo.getAgentStartTime(), sqlUidMetaDataBo.getApplicationName(), sqlUidMetaDataBo.getUid(), sql);
        }
        this.sqlUidMetaDataDao.insert(sqlUidMetaDataBo);
    }
}
