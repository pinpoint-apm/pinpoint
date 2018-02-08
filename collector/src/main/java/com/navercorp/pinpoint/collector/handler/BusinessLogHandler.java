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

package com.navercorp.pinpoint.collector.handler;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.collector.dao.BusinessLogDao;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.BusinessLogBatchMapper;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogBo;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogV1Bo;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogBatch;

/**
 * [XINGUANG]
 */
@Service("businessLogHandler")
public class BusinessLogHandler implements SimpleHandler{
	
	private final Logger logger = LoggerFactory.getLogger(BusinessLogHandler.class);
	
	@Autowired
    private BusinessLogBatchMapper businessLogBatchMapper;

	@Autowired
	private BusinessLogDao<BusinessLogV1Bo> businessLogDao;

    private void handleBusinessLogBatch(TBusinessLogBatch tBusinessLogBatch) {
    	if (logger.isDebugEnabled()) {
            logger.debug("Received TBusinessLogBatch={}", tBusinessLogBatch);
        }
    	
    	BusinessLogBo businessLogBo = this.businessLogBatchMapper.map(tBusinessLogBatch);
    	
    	if(businessLogBo == null) {
    		return;
    	}
    	businessLogDao.insert(businessLogBo.getAgentId(), businessLogBo.getBusinessLogs());
    }

	@Override
	public void handleSimple(TBase<?, ?> tBase) {
		if(tBase instanceof TBusinessLogBatch) {
			TBusinessLogBatch tBusinessLogBatch = (TBusinessLogBatch) tBase;
			this.handleBusinessLogBatch(tBusinessLogBatch);
		}else {
			throw new IllegalArgumentException("unexpected tbase:" + tBase + " expected:" + TBusinessLogBatch.class.getName());
		}
	}
}
