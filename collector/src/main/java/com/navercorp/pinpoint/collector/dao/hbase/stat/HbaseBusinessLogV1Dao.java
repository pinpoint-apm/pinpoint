/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.stat;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.collector.dao.BusinessLogDao;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.BusinessLogHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.BusinessLogV1Serializer;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogV1Bo;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogType;

/**
 * [XINGUANG]
 */
@Repository
public class HbaseBusinessLogV1Dao implements BusinessLogDao<BusinessLogV1Bo>{
	
	@Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private BusinessLogHbaseOperationFactory businessLogHbaseOperationFactory;

    @Autowired
    private BusinessLogV1Serializer businessLogV1Serializer;

    @Override
	public void insert(String agentId, List<BusinessLogV1Bo> businessLogV1Bo) {
		if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
		if (businessLogV1Bo == null) {
			return;
		}
				
		List<Put> businessLogPut = businessLogHbaseOperationFactory.createPuts(agentId, BusinessLogType.BUSINESS_LOG_V1, businessLogV1Bo, this.businessLogV1Serializer);
		if (!businessLogPut.isEmpty()) {
            List<Put> rejectedPuts = this.hbaseTemplate.asyncPut(HBaseTables.BUSINESS_LOG, businessLogPut);
            if (CollectionUtils.isNotEmpty(rejectedPuts)) {
                this.hbaseTemplate.put(HBaseTables.BUSINESS_LOG, rejectedPuts);
            }
        }
	}
}
