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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogV1Bo;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogBo;
import com.navercorp.pinpoint.thrift.dto.TBusinessLog;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogBatch;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogV1;

/**
 * [XINGUANG]
 */
@Component
public class BusinessLogBatchMapper implements ThriftBoMapper<BusinessLogBo, TBusinessLogBatch>{

	@Override
	public BusinessLogBo map(TBusinessLogBatch TBusinessLogBatch) {
		if (!TBusinessLogBatch.isSetBusinessLogs()) {
			return null;
		}
		BusinessLogBo businessLogBo = new BusinessLogBo();
		String agentId = TBusinessLogBatch.getAgentId();
		long startTimestamp = TBusinessLogBatch.getStartTimestamp();
		businessLogBo.setAgentId(agentId);
		businessLogBo.setStartTimestamp(startTimestamp);
		
		int size = TBusinessLogBatch.getBusinessLogsSize();

		List<BusinessLogV1Bo> businessLogs = new ArrayList<BusinessLogV1Bo>(size);

		for(TBusinessLog tBusinessLog : TBusinessLogBatch.getBusinessLogs()) {			
			long timestamp = tBusinessLog.getTimestamp();
			for(TBusinessLogV1 tBusinessLogV1 : tBusinessLog.getBusinessLogV1s()) {
				BusinessLogV1Bo BusinessLogV1Bo = new BusinessLogV1Bo();
				BusinessLogV1Bo.setTime(tBusinessLogV1.getTime());
				BusinessLogV1Bo.setThreadName(tBusinessLogV1.getThreadName());
				BusinessLogV1Bo.setLogLevel(tBusinessLogV1.getLogLevel());
				BusinessLogV1Bo.setClassName(tBusinessLogV1.getClassName());
				BusinessLogV1Bo.setMessage(tBusinessLogV1.getMessage());
				BusinessLogV1Bo.setTransactionId(tBusinessLogV1.getTransactionId());
				BusinessLogV1Bo.setSpanId(tBusinessLogV1.getSpanId());
				
				BusinessLogV1Bo.setAgentId(agentId);
				BusinessLogV1Bo.setStartTimestamp(startTimestamp);
				BusinessLogV1Bo.setTimestamp(timestamp);
	
				businessLogs.add(BusinessLogV1Bo);
			}
		}
 		businessLogBo.setBusinessLogs(businessLogs);
		return businessLogBo;
	}

}
