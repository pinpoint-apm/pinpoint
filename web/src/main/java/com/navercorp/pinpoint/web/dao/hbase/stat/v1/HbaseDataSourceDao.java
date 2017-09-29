/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao.hbase.stat.v1;

import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.web.dao.stat.DataSourceDao;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Deprecated
@Repository("dataSourceDaoV1")
public class HbaseDataSourceDao implements DataSourceDao {

    // not support v1
    @Override
    public List<DataSourceListBo> getAgentStatList(String agentId, Range range) {
        return Collections.emptyList();
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        return false;
    }

}
